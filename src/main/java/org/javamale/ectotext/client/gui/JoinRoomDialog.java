package org.javamale.ectotext.client.gui;

import org.javamale.ectotext.client.GameClient;
import org.javamale.ectotext.client.network.SocketClient;
import org.javamale.ectotext.client.rest.RestAccess;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Collection;

/**
 * Finestra di dialogo per l'accesso ad una stanza di gioco esistente.
 * Questa finestra modale permette a un utente di:
 * <ul>
 *   <li>Visualizzare i giocatori attualmente presenti nella stanza</li>
 *   <li>Inserire il proprio nome utente</li>
 *   <li>Fornire la password della stanza</li>
 *   <li>Tentare la connessione alla stanza</li>
 * </ul>
 * <p>
 * La finestra gestisce in modo asincrono sia il recupero della lista utenti
 * che il tentativo di connessione, per mantenere l'interfaccia reattiva.
 * </p>
 */
public class JoinRoomDialog extends JDialog {
    /** Nome della stanza a cui si sta tentando di accedere. */
    private final String roomName;

    /** Modello dati per la lista degli utenti nella stanza. */
    private final DefaultListModel<String> userListModel = new DefaultListModel<>();
    /** Lista degli utenti attualmente presenti nella stanza. */
    private final JList<String> userList = new JList<>(userListModel);
    /** Campo per l'inserimento del nome del giocatore. */
    private final JTextField playerNameField = new JTextField(20);
    /** Campo per l'inserimento della password della stanza. */
    private final JPasswordField roomPasswordField = new JPasswordField(20);
    /** Pulsante per avviare il tentativo di connessione. */
    private final JButton joinButton = new JButton("Connetti");

    /**
     * Crea una nuova finestra di dialogo per l'accesso a una stanza.
     * 
     * @param parent la finestra principale del gioco a cui questa finestra è associata
     * @param roomName il nome della stanza a cui tentare l'accesso
     */
    public JoinRoomDialog(GameWindow parent, String roomName) {
        super(parent, "Entra in " + roomName, true);
        this.roomName = roomName;

        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(25, 25, 25));

        Dimension fieldSize = new Dimension(200, 30);
        playerNameField.setPreferredSize(fieldSize);
        playerNameField.setMaximumSize(fieldSize);
        roomPasswordField.setPreferredSize(fieldSize);
        roomPasswordField.setMaximumSize(fieldSize);

        add(createUserPanel(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
        attemptRetriveUsers();

        playerNameField.addActionListener(e -> roomPasswordField.requestFocus());
        roomPasswordField.addActionListener(e -> joinButton.doClick());
    }

    /**
     * Crea il pannello contenente la lista degli utenti.
     * 
     * @return pannello configurato con la lista degli utenti
     */
    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(25, 25, 25));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Giocatori nella stanza",
                TitledBorder.LEFT, TitledBorder.TOP, null, Color.LIGHT_GRAY
        ));

        userList.setBackground(new Color(40, 40, 40));
        userList.setForeground(Color.WHITE);
        panel.add(new JScrollPane(userList), BorderLayout.CENTER);
        return panel;
    }

    /**
     * Crea il pannello contenente i campi del form di accesso.
     * 
     * @return pannello configurato con i campi di input
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(25, 25, 25));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Dati Accesso",
                TitledBorder.LEFT, TitledBorder.TOP, null, Color.LIGHT_GRAY
        ));

        panel.add(createField("Nome Giocatore:", playerNameField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createPasswordField("Password Stanza:", roomPasswordField));

        return panel;
    }

    /**
     * Crea il pannello inferiore contenente il pulsante di connessione.
     * 
     * @return pannello configurato con il pulsante di connessione
     */
    private JPanel createBottomPanel() {
        styleButton(joinButton);
        joinButton.addActionListener(e -> attemptJoin());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(new Color(25, 25, 25));
        panel.add(joinButton);
        return panel;
    }

    /**
     * Crea un campo di input con etichetta usando un layout a griglia.
     * 
     * @param labelText il testo dell'etichetta per il campo
     * @param field il campo di input da includere
     * @return pannello contenente l'etichetta e il campo di input
     */
    private JPanel createField(String labelText, JTextField field) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(25, 25, 25));

        JLabel label = new JLabel(labelText);
        label.setForeground(Color.LIGHT_GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(field, gbc);

        return panel;
    }

    /**
     * Crea un campo password con etichetta e pulsante di visualizzazione.
     * 
     * @param labelText il testo dell'etichetta per il campo
     * @param field il campo password da includere
     * @return pannello contenente l'etichetta, il campo password e il pulsante di visualizzazione
     */
    private JPanel createPasswordField(String labelText, JPasswordField field) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(25, 25, 25));

        JLabel label = new JLabel(labelText);
        label.setForeground(Color.LIGHT_GRAY);

        JToggleButton toggle = new JToggleButton("👁️");
        toggle.setPreferredSize(new Dimension(40, 30));
        toggle.setFocusPainted(false);
        toggle.setBackground(new Color(50, 50, 50));
        toggle.setForeground(Color.LIGHT_GRAY);
        toggle.setBorder(BorderFactory.createEmptyBorder());
        toggle.addActionListener(e -> field.setEchoChar(toggle.isSelected() ? (char) 0 : '•'));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(field, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(toggle, gbc);

        return panel;
    }

    /**
     * Applica lo stile visuale standard ai pulsanti.
     * Configura colori, font, bordi ed effetti hover.
     * 
     * @param b il pulsante da stilizzare
     */
    private void styleButton(JButton b) {
        b.setBackground(new Color(80, 0, 0));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(100, 30));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createLineBorder(new Color(255, 40, 40)));
        b.setRolloverEnabled(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(true);
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b.setBackground(new Color(100, 0, 0)); // Hover color
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                b.setBackground(new Color(80, 0, 0)); // Original color
            }
        });
    }

    /**
     * Aggiorna la lista degli utenti connessi alla stanza.
     * 
     * @param users collezione degli utenti da visualizzare nella lista
     */
    public void populateUserList(Collection<String> users) {
        userListModel.clear();
        userListModel.addAll(users);
    }

    /**
     * Avvia il recupero asincrono della lista degli utenti nella stanza.
     * In caso di errore di comunicazione con il server, mostra un messaggio di errore.
     */
    public void attemptRetriveUsers() {
        GameClient client = GameClient.getInstance();
        new Thread(() -> {
            if (!client.isRestHealthy()) {
                showError("Server non raggiungibile");
                return;
            }
            var users = RestAccess.retriveRoomUsers(client.getServerAddress(), client.getServerRestPort(), roomName);
            if (users != null) {
                SwingUtilities.invokeLater(() -> populateUserList(users));
            }
        }).start();
    }

    /**
     * Tenta di unirsi alla stanza utilizzando le credenziali inserite.
     * <p>
     * Il processo include:
     * <ul>
     *   <li>Validazione dei campi di input</li>
     *   <li>Connessione al server socket se necessario</li>
     *   <li>Tentativo di accesso alla stanza</li>
     * </ul>
     * Durante il processo, il pulsante di connessione viene disabilitato e
     * mostra lo stato della connessione.
     */
    public void attemptJoin() {
        String name = playerNameField.getText().trim();
        String pass = new String(roomPasswordField.getPassword()).trim();

        if (name.isEmpty()) {
            showError("Inserisci un nome valido");
            return;
        }

        joinButton.setEnabled(false);
        joinButton.setText("Connessione...");

        new Thread(() -> {
            GameClient client = GameClient.getInstance();
            GameWindow window = client.getGameWindow();

            if (!client.isConnected()) {
                var host = client.getServerAddress();
                var port = client.getServerRestPort();

                if (!client.isRestHealthy()) {
                    showError("Server non raggiungibile");
                    return;
                }

                var socketPort = RestAccess.retrieveSocketPort(host, port);
                SocketClient socketClient = client.connect(host, socketPort);
                if (socketClient == null) {
                    showError("Connessione fallita");
                    return;
                }
            }

            client.joinRoom(name, roomName, pass).thenAccept(success -> {
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        dispose();
                        window.showGamePanel();
                    } else {
                        joinButton.setText("Connetti");
                        joinButton.setEnabled(true);
                        showError("Connessione non riuscita");
                    }
                });
            });
        }).start();
    }

    /**
     * Mostra un messaggio di errore in una finestra di dialogo.
     * 
     * @param msg il messaggio di errore da visualizzare
     */
    public void showError(String msg) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, msg, "Errore", JOptionPane.ERROR_MESSAGE));
    }
}