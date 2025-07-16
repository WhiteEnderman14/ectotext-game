package org.javamale.ectotext.client.gui.panel;

import org.javamale.ectotext.client.GameClient;
import org.javamale.ectotext.client.gui.CreateRoomDialog;
import org.javamale.ectotext.client.gui.JoinRoomDialog;
import org.javamale.ectotext.client.gui.GameWindow;
import org.javamale.ectotext.client.rest.RestAccess;
import org.javamale.ectotext.common.packet.impl.RoomListPacket;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.Collection;

/**
 * Pannello che gestisce la visualizzazione e l'interazione con la lobby di gioco.
 * Fornisce un'interfaccia utente per:
 * <ul>
 *   <li>Visualizzare la lista delle stanze disponibili</li>
 *   <li>Creare nuove stanze di gioco</li>
 *   <li>Unirsi a stanze esistenti</li>
 *   <li>Aggiornare la lista delle stanze</li>
 *   <li>Controllare la riproduzione della musica</li>
 * </ul>
 */
public class LobbyPanel extends JPanel {
    /** Tabella che visualizza la lista delle stanze disponibili. */
    private final JTable roomList;
    
    /** Modello dati per la tabella delle stanze. */
    private final DefaultTableModel roomListModel;
    
    /** Pulsante per aggiornare la lista delle stanze. */
    private final JButton refreshButton;
    
    /** Pulsante per creare una nuova stanza. */
    private final JButton addButton;
    
    /** Pulsante per unirsi alla stanza selezionata. */
    private final JButton connectButton;
    
    /** Etichetta per il controllo dello stato della musica. */
    private final JLabel musicStatusLabel;
    
    /** Riferimento alla finestra principale del gioco. */
    private final GameWindow gameWindow;

    /**
     * Crea un nuovo pannello della lobby.
     * Inizializza l'interfaccia utente con:
     * <ul>
     *   <li>Una tabella per la lista delle stanze</li>
     *   <li>Pulsanti per le azioni principali</li>
     *   <li>Controllo per la musica di sottofondo</li>
     * </ul>
     *
     * @param gameWindow la finestra principale del gioco
     */
    public LobbyPanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;

        setBackground(new Color(30, 30, 30));
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("🎮 Lobby", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 22));
        title.setForeground(new Color(255, 40, 40));
        add(title, BorderLayout.NORTH);

        roomListModel = new DefaultTableModel(new String[]{"Nome", "Giocatori"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        roomList = new JTable(roomListModel);
        roomList.setBackground(new Color(45, 45, 45));
        roomList.setForeground(Color.WHITE);
        roomList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        roomList.setRowHeight(40);

        roomList.getColumnModel().getColumn(0).setPreferredWidth(200);
        roomList.getColumnModel().getColumn(1).setPreferredWidth(100);

        roomList.setDefaultRenderer(Object.class, (table, value, isSelected, hasFocus, row, column) -> {
            JLabel label = new JLabel(value.toString());
            label.setForeground(Color.WHITE);
            label.setFont(new Font("SansSerif", Font.PLAIN, 14));
            label.setOpaque(true);

            if (isSelected) {
                label.setBackground(new Color(180, 40, 40));
            } else {
                label.setBackground(row % 2 == 0 ? new Color(50, 50, 50) : new Color(40, 40, 40));
            }
            label.setHorizontalAlignment(SwingConstants.CENTER);
            return label;
        });

        JScrollPane scroll = new JScrollPane(roomList);
        add(scroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 5));
        buttonPanel.setOpaque(false);

        refreshButton = new JButton("🔄 Aggiorna");
        addButton = new JButton("➕ Crea");
        connectButton = new JButton("➡️ Entra");
        musicStatusLabel = new JLabel("🔇", SwingConstants.CENTER);

        for (JButton b : new JButton[]{refreshButton, addButton, connectButton}) {
            styleButton(b);
        }

        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(connectButton);

        JPanel musicPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        musicPanel.setOpaque(false);
        musicStatusLabel.setFont(new Font("Monospaced", Font.BOLD, 40));
        musicStatusLabel.setForeground(Color.WHITE);
        musicStatusLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        musicPanel.add(musicStatusLabel);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        southPanel.add(musicPanel, BorderLayout.EAST);

        add(southPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> attemptRoomListRefresh());
        addButton.addActionListener(e -> attemptRoomCreationAndJoin());
        connectButton.addActionListener(e -> attemptRoomJoin());

        musicStatusLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                gameWindow.toggleMusic();
                updateMusicStatus();
            }
        });

        updateMusicStatus();
    }

    /**
     * Applica lo stile visuale standard ai pulsanti del pannello.
     * Configura colori, font, dimensioni e bordi.
     *
     * @param b il pulsante da stilizzare
     */
    private void styleButton(JButton b) {
        b.setBackground(new Color(80, 0, 0));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(120, 35));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createLineBorder(new Color(255, 40, 40)));
    }

    /**
     * Aggiorna la tabella delle stanze con i dati più recenti.
     * Per ogni stanza visualizza:
     * <ul>
     *   <li>Nome della stanza</li>
     *   <li>Numero di giocatori presenti su 3 massimi</li>
     * </ul>
     *
     * @param rooms collezione di informazioni sulle stanze da visualizzare
     */
    public void populateRoomList(Collection<RoomListPacket.RoomListEntry> rooms) {
        roomListModel.setRowCount(0);
        for (var e : rooms) {
            String label = String.format("%s", e.name());
            String players = String.format("%d/3", e.userCount());
            roomListModel.addRow(new Object[]{label, players});
        }
    }

    /**
     * Richiede al server la lista aggiornata delle stanze.
     * <p>
     * Esegue la richiesta in un thread separato per non bloccare l'interfaccia.
     * In caso di errore di connessione, mostra un messaggio appropriato.
     * </p>
     */
    public void attemptRoomListRefresh() {
        GameClient gameClient = GameClient.getInstance();
        GameWindow gameWindow = gameClient.getGameWindow();

        new Thread(() -> {
            if (!gameClient.isRestHealthy()) {
                SwingUtilities.invokeLater(() -> gameWindow.showError("Server non raggiungibile"));
                return;
            }
            var rooms = RestAccess.retrieveRoomList(gameClient.getServerAddress(), gameClient.getServerRestPort());
            if (rooms != null) {
                SwingUtilities.invokeLater(() -> populateRoomList(rooms));
            }
        }).start();
    }

    /**
     * Avvia il processo di accesso a una stanza esistente.
     * <p>
     * Se una stanza è selezionata nella lista, apre il dialogo di accesso.
     * Altrimenti, mostra un messaggio di errore.
     * </p>
     */
    public void attemptRoomJoin() {
        int row = roomList.getSelectedRow();
        if (row < 0) {
            GameClient.getInstance().getGameWindow().showError("Seleziona prima una stanza");
            return;
        }
        String roomName = roomListModel.getValueAt(row, 0).toString();
        new JoinRoomDialog(GameClient.getInstance().getGameWindow(), roomName).setVisible(true);
    }

    /**
     * Avvia il processo di creazione di una nuova stanza.
     * Apre il dialogo per la configurazione della nuova stanza.
     */
    public void attemptRoomCreationAndJoin() {
        new CreateRoomDialog(GameClient.getInstance().getGameWindow()).setVisible(true);
    }

    /**
     * Aggiorna l'icona dello stato della musica.
     * Mostra 🔊 quando la musica è attiva, 🔇 quando è disattivata.
     */
    public void updateMusicStatus() {
        if (gameWindow.getMusicPlaying()) {
            musicStatusLabel.setText("🔊");
        } else {
            musicStatusLabel.setText("🔇");
        }
    }
}