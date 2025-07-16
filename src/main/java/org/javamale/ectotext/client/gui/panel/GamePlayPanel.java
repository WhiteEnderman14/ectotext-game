package org.javamale.ectotext.client.gui.panel;

import org.javamale.ectotext.client.GameClient;
import org.javamale.ectotext.client.gui.GameWindow;
import org.javamale.ectotext.client.gui.MapDialog;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Pannello principale di gioco che gestisce l'interfaccia durante la sessione di gioco.
 * 
 * <p>Questo pannello contiene:
 * <ul>
 *   <li>Area di gioco testuale per la visualizzazione dei messaggi</li>
 *   <li>Campo di input per i comandi di gioco</li>
 *   <li>Lista degli utenti connessi</li>
 *   <li>Area di chat con campo di input dedicato</li>
 *   <li>Barra di navigazione con pulsanti per:
 *     <ul>
 *       <li>Selezione personaggio (Ghostbusters)</li>
 *       <li>Visualizzazione mappa</li>
 *       <li>Controllo musica</li>
 *       <li>Gestione stanza (disconnessione/eliminazione)</li>
 *     </ul>
 *   </li>
 * </ul>
 */
public class GamePlayPanel extends JPanel {

    /**
     * Area di testo per la visualizzazione dei messaggi di gioco.
     */
    private final JTextPane gamePane;

    /**
     * Campo di input per i comandi di gioco.
     */
    private final JTextField commandField;

    /**
     * Pulsante per l'invio dei comandi di gioco.
     */
    private final JButton sendCommandButton;

    /**
     * Lista per la visualizzazione degli utenti connessi.
     */
    private final JList<String> userList;

    /**
     * Modello dati per la lista degli utenti.
     */
    private final DefaultListModel<String> userListModel;

    /**
     * Area di testo per la visualizzazione dei messaggi di chat.
     */
    private final JTextArea chatArea;

    /**
     * Campo di input per i messaggi di chat.
     */
    private final JTextField chatField;

    /**
     * Pulsante per l'invio dei messaggi di chat.
     */
    private final JButton sendChatButton;

    /**
     * Pulsante per disconnettersi dalla stanza.
     */
    private final JButton disconnectButton;

    /**
     * Pulsante per eliminare la stanza corrente.
     */
    private final JButton deleteRoomButton;

    /**
     * Pulsante per visualizzare la mappa.
     */
    private final JButton mapButton;

    /**
     * Pannello per la barra di navigazione.
     */
    private final JPanel navBar;

    /**
     * Etichetta per lo stato della musica.
     */
    private final JLabel musicStatusLabel;

    /**
     * Pulsante per selezionare il personaggio Peter.
     */
    private final JToggleButton peterButton;

    /**
     * Pulsante per selezionare il personaggio Ray.
     */
    private final JToggleButton rayButton;

    /**
     * Pulsante per selezionare il personaggio Egon.
     */
    private final JToggleButton egonButton;

    /**
     * Pannello contenente i pulsanti dei personaggi Ghostbusters.
     */
    private final JPanel ghostbustersPanel;

    /**
     * Riferimento alla finestra principale di gioco.
     */
    private final GameWindow gameWindow;

    /**
     * Coda per la gestione della stampa progressiva dei messaggi.
     */
    private final BlockingQueue<Runnable> printQueue = new LinkedBlockingQueue<>();

    /**
     * Flag che indica se è in corso la stampa di messaggi.
     */
    private volatile boolean printingInProgress = false;

    /**
     * Flag che indica se è abilitata la stampa progressiva dei caratteri.
     */
    private final boolean progressivePrintEnabled = true;

    /**
     * Costruisce un nuovo pannello di gioco.
     *
     * @param gameWindow riferimento alla finestra principale di gioco
     */
    public GamePlayPanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;

        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(30, 30, 30));
        setBorder(new EmptyBorder(8,8,8,8));

        // NavBar - Layout responsive
        navBar = new JPanel(new GridBagLayout());
        navBar.setBackground(new Color(50, 50, 50));
        navBar.setBorder(new EmptyBorder(4, 8, 4, 8));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 8, 0, 8);

        // Pannello Ghostbusters
        ghostbustersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        ghostbustersPanel.setBackground(new Color(50, 50, 50));

        peterButton = new JToggleButton("Peter");
        rayButton = new JToggleButton("Ray");
        egonButton = new JToggleButton("Egon");

        peterButton.setEnabled(false);
        rayButton.setEnabled(false);
        egonButton.setEnabled(false);

        styleCharacterButton(peterButton);
        styleCharacterButton(rayButton);
        styleCharacterButton(egonButton);

        ghostbustersPanel.add(peterButton);
        ghostbustersPanel.add(rayButton);
        ghostbustersPanel.add(egonButton);

        peterButton.addActionListener(e -> attemptSelectCharacter("peter"));
        rayButton.addActionListener(e -> attemptSelectCharacter("ray"));
        egonButton.addActionListener(e -> attemptSelectCharacter("egon"));

        // Pulsante Mappa
        mapButton = new JButton(" Mappa");
        styleMapButton(mapButton);
        try {
            ImageIcon mapIcon = new ImageIcon(ImageIO.read(new File("icons/map_icon.png"))
                    .getScaledInstance(20, 20, Image.SCALE_SMOOTH));
            mapButton.setIcon(mapIcon);
        } catch (Exception ignored) {}
        mapButton.addActionListener(e -> openMapDialog());

        // Pulsante Disconnetti
        disconnectButton = new JButton("Disconnetti");
        styleDangerButton(disconnectButton);
        disconnectButton.addActionListener(e -> {
            GameClient.getInstance().disconnectRoom();
            clearGamePane();
            clearChatArea();
            gameWindow.showLobbyPanel();
            resetCharacterButtons();
        });

        // Pulsante Cancella Stanza
        deleteRoomButton = new JButton("Cancella Stanza");
        styleDangerButton(deleteRoomButton);
        deleteRoomButton.addActionListener(e -> {
            UIManager.put("Panel.background", new Color(30, 30, 30));
            UIManager.put("OptionPane.background", new Color(30, 30, 30));
            UIManager.put("OptionPane.messageForeground", Color.WHITE);
            UIManager.put("Button.background", new Color(150, 0, 0));
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("OptionPane.foreground", Color.WHITE);
            UIManager.put("OptionPane.messageFont", new Font("SansSerif", Font.PLAIN, 13));
            UIManager.put("Button.font", new Font("SansSerif", Font.BOLD, 12));

            int result = JOptionPane.showConfirmDialog(
                    this,
                    "Sei sicuro di voler cancellare la stanza?\nL'azione non può essere annullata.\nIl salvataggio della partita verrá perso.",
                    "Conferma Eliminazione",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                GameClient.getInstance().deleteRoom();
                clearGamePane();
                clearChatArea();
                gameWindow.showLobbyPanel();
                resetCharacterButtons();
            }
        });

        // Etichetta per lo stato della musica (emoji)
        musicStatusLabel = new JLabel("🔇", SwingConstants.CENTER);  // Emoji iniziale per la musica in esecuzione
        musicStatusLabel.setFont(new Font("Monospaced", Font.BOLD, 40));
        musicStatusLabel.setForeground(Color.WHITE);
        musicStatusLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        musicStatusLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                gameWindow.toggleMusic();  // Alterna la musica
                updateMusicStatus();  // Aggiorna l'emoji in base allo stato della musica
            }
        });

        // Pannello musica
        JPanel musicPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        musicPanel.setOpaque(false);
        musicPanel.add(musicStatusLabel);

        // Aggiungi il pannello di Ghostbusters e altri componenti nella navBar
        gbc.gridx = 0; gbc.gridy = 0;
        navBar.add(ghostbustersPanel, gbc);

        gbc.gridx = 1;
        navBar.add(mapButton, gbc);

        gbc.gridx = 2;
        navBar.add(musicPanel, gbc);

        gbc.gridx = 3;
        navBar.add(deleteRoomButton, gbc);

        gbc.gridx = 4;
        navBar.add(disconnectButton, gbc);

        // Aggiungi la navBar alla finestra principale
        add(navBar, BorderLayout.NORTH);

        // Pannello centrale (gameplay, chat, etc.)
        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setBackground(new Color(30, 30, 30));

        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.setBackground(new Color(30, 30, 30));
        leftPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        gamePane = new JTextPane();
        gamePane.setEditable(false);
        gamePane.setBackground(new Color(20, 20, 20));
        gamePane.setForeground(Color.WHITE);
        gamePane.setFont(new Font("Consolas", Font.PLAIN, 14));
        JScrollPane gameScroll = new JScrollPane(gamePane);
        leftPanel.add(gameScroll, BorderLayout.CENTER);

        JPanel commandPanel = new JPanel(new BorderLayout(4, 0));
        commandPanel.setBackground(new Color(30, 30, 30));
        commandField = new JTextField();
        sendCommandButton = new JButton("Invia");
        styleNormalButton(sendCommandButton);
        commandPanel.add(commandField, BorderLayout.CENTER);
        commandPanel.add(sendCommandButton, BorderLayout.EAST);

        sendCommandButton.addActionListener(e -> attemptSendCommand());
        commandField.addActionListener(e -> sendCommandButton.doClick());

        leftPanel.add(commandPanel, BorderLayout.SOUTH);

        centerPanel.add(leftPanel, BorderLayout.CENTER);

        // Pannello destro per la lista degli utenti e la chat
        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.setPreferredSize(new Dimension(320, 0));
        rightPanel.setBackground(new Color(30, 30, 30));
        rightPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setBackground(new Color(50, 50, 50));
        userList.setForeground(Color.WHITE);
        userList.setFont(new Font("SansSerif", Font.PLAIN, 13));
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel userListPanel = new JPanel(new BorderLayout());
        userListPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Giocatori",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 14),
                Color.DARK_GRAY));
        userListPanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        rightPanel.add(userListPanel, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(50, 50, 50));
        chatArea.setForeground(Color.CYAN);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Chat",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 14),
                Color.DARK_GRAY));
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        rightPanel.add(chatPanel, BorderLayout.CENTER);

        JPanel chatInputPanel = new JPanel(new BorderLayout(4, 4));
        chatInputPanel.setBackground(new Color(30, 30, 30));

        chatField = new JTextField();
        sendChatButton = new JButton("Chat");
        styleNormalButton(sendChatButton);
        sendChatButton.addActionListener(e -> attemptSendChatMessage());
        chatField.addActionListener(e -> sendChatButton.doClick());

        chatInputPanel.add(chatField, BorderLayout.CENTER);
        chatInputPanel.add(sendChatButton, BorderLayout.EAST);
        rightPanel.add(chatInputPanel, BorderLayout.SOUTH);

        centerPanel.add(rightPanel, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Applica lo stile standard ai pulsanti normali.
     *
     * @param b pulsante a cui applicare lo stile
     */
    private void styleNormalButton(JButton b) {
        b.setBackground(new Color(80, 0, 0));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(100, 30));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createLineBorder(new Color(255, 40, 40)));
    }

    /**
     * Applica lo stile per i pulsanti di azione pericolosa.
     *
     * @param b pulsante a cui applicare lo stile
     */
    private void styleDangerButton(JButton b) {
        b.setBackground(new Color(180, 0, 0));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(100, 30));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createLineBorder(new Color(255, 40, 40)));
    }

    /**
     * Applica lo stile per il pulsante della mappa.
     *
     * @param b pulsante a cui applicare lo stile
     */
    private void styleMapButton(JButton b) {
        b.setBackground(new Color(0, 100, 0));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(100, 30));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 0)));
    }

    /**
     * Applica lo stile per i pulsanti di selezione personaggio.
     *
     * @param b pulsante a cui applicare lo stile
     */
    private void styleCharacterButton(JToggleButton b) {
        b.setBackground(new Color(0, 0, 150));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
    }

    /**
     * Pulisce l'area di gioco dai messaggi.
     */
    private void clearGamePane() {
        printingInProgress = false;
        printQueue.clear();

        SwingUtilities.invokeLater(() -> {
            gamePane.setText("");
        });
    }

    /**
     * Pulisce l'area della chat dai messaggi.
     */
    private void clearChatArea() {
        SwingUtilities.invokeLater(() -> {
            chatArea.setText("");
        });
    }

    /**
     * Aggiunge un messaggio all'area di gioco con stile specifico.
     *
     * @param text testo da aggiungere
     * @param color colore del testo
     * @param bold true se il testo deve essere in grassetto
     * @param italic true se il testo deve essere in corsivo
     */
    private void appendToGamePane(String text, Color color, boolean bold, boolean italic) {
        printQueue.add(() -> {
            try {
                StyledDocument doc = gamePane.getStyledDocument();
                Style style = gamePane.addStyle("messageStyle", null);
                StyleConstants.setForeground(style, color);
                StyleConstants.setBold(style, bold);
                StyleConstants.setItalic(style, italic);

                if (progressivePrintEnabled) {
                    for (char c : text.toCharArray()) {
                        SwingUtilities.invokeAndWait(() -> {
                            try {
                                doc.insertString(doc.getLength(), String.valueOf(c), style);
                                gamePane.setCaretPosition(doc.getLength());
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        });
                        Thread.sleep(15);
                    }
                } else {
                    SwingUtilities.invokeAndWait(() -> {
                        try {
                            doc.insertString(doc.getLength(), text, style);
                            gamePane.setCaretPosition(doc.getLength());
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        startPrintingQueue();
    }

    /**
     * Avvia il processo di stampa dei messaggi in coda.
     */
    private synchronized void startPrintingQueue() {
        if (printingInProgress) return;
        printingInProgress = true;

        new Thread(() -> {
            while (printingInProgress && !printQueue.isEmpty()) {
                Runnable task = printQueue.poll();
                if (task != null) {
                    task.run();
                }
            }
            printingInProgress = false;
        }).start();
    }

    /**
     * Apre la finestra di dialogo della mappa.
     */
    private void openMapDialog() {
        MapDialog dialog = new MapDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }

    /**
     * Tenta di inviare un messaggio di chat.
     * Se il messaggio non è vuoto, viene inviato in un thread separato attraverso il GameClient.
     */
    public void attemptSendChatMessage() {
        String msg = chatField.getText().trim();
        chatField.setText("");
        if (!msg.isEmpty()) new Thread(() -> GameClient.getInstance().sendChatMessage(msg)).start();
    }

    /**
     * Visualizza un messaggio di chat ricevuto nell'area dedicata.
     *
     * @param sender mittente del messaggio
     * @param message contenuto del messaggio
     */
    public void receiveChatMessage(String sender, String message) {
        chatArea.append(sender + ": " + message + "\n");
    }

    /**
     * Aggiorna la lista degli utenti connessi nella stanza.
     * Pulisce la lista corrente e aggiunge i nuovi utenti.
     *
     * @param users collezione degli utenti da visualizzare
     */
    public void receiveUserList(Collection<String> users) {
        userListModel.clear();
        userListModel.addAll(users);
    }

    /**
     * Tenta di selezionare un personaggio per il giocatore corrente.
     * Resetta la selezione precedente e invia la richiesta al server.
     *
     * @param character nome del personaggio da selezionare ("peter", "ray" o "egon")
     */
    public void attemptSelectCharacter(String character) {
        resetCharacterButtons();
        switch (character) {
            case "peter" -> peterButton.setSelected(true);
            case "ray" -> rayButton.setSelected(true);
            case "egon" -> egonButton.setSelected(true);
        }
        new Thread(() -> GameClient.getInstance().selectCharacter(character)).start();
    }

    /**
     * Reimposta lo stato dei pulsanti di selezione personaggio.
     */
    private void resetCharacterButtons() {
        peterButton.setSelected(false);
        rayButton.setSelected(false);
        egonButton.setSelected(false);
    }

    /**
     * Imposta i personaggi disponibili per la selezione.
     *
     * @param availableCharacters collezione dei personaggi disponibili
     */
    public void setCharacterButtons(Collection<String> availableCharacters) {
        peterButton.setEnabled(false);
        rayButton.setEnabled(false);
        egonButton.setEnabled(false);

        for (String character : availableCharacters) {
            switch (character) {
                case "peter" -> peterButton.setEnabled(true);
                case "ray" -> rayButton.setEnabled(true);
                case "egon" -> egonButton.setEnabled(true);
            }
        }
    }

    /**
     * Visualizza un messaggio del narratore nell'area di gioco.
     *
     * @param message messaggio da visualizzare
     */
    public void receiveNarratorMessage(String message) {
        appendToGamePane("[Narratore] " + message + "\n", Color.GRAY, false, false);
    }

    /**
     * Visualizza un dialogo nell'area di gioco.
     *
     * @param speaker personaggio che parla
     * @param message contenuto del dialogo
     */
    public void receiveDialogueMessage(String speaker, String message) {
        appendToGamePane(speaker + ": ", Color.ORANGE, true, false);
        appendToGamePane(message + "\n", Color.LIGHT_GRAY, false, false);
    }

    /**
     * Visualizza un messaggio di errore nell'area di gioco.
     *
     * @param message messaggio di errore da visualizzare
     */
    public void receiveErrorMessage(String message) {
        appendToGamePane("[Errore] " + message + "\n", Color.RED, false, true);
    }

    /**
     * Tenta di inviare un comando di gioco.
     */
    public void attemptSendCommand() {
        String msg = commandField.getText().trim();
        commandField.setText("");
        if (!msg.isEmpty()) new Thread(() -> GameClient.getInstance().sendGameCommand(msg)).start();
    }

    /**
     * Pulisce tutte le aree di chat e gioco.
     */
    public void clearAllChat() {
        clearGamePane();
        clearChatArea();
    }

    /**
     * Aggiorna l'icona dello stato della musica.
     */
    public void updateMusicStatus() {
        if (gameWindow.getMusicPlaying()) {
            musicStatusLabel.setText("🔊");  // Emoji per la musica in esecuzione
        } else {
            musicStatusLabel.setText("🔇");  // Emoji per la musica mutata (spenta)
        }
    }
}