package org.javamale.ectotext.client.gui;

import org.javamale.ectotext.client.GameClient;
import org.javamale.ectotext.client.network.SocketClient;
import org.javamale.ectotext.client.rest.RestAccess;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import javax.swing.text.PlainDocument;
import javax.swing.text.DocumentFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

/**
 * Finestra di dialogo per la creazione di una nuova stanza di gioco.
 * Questa finestra modale permette a un utente di:
 * <ul>
 *   <li>Inserire il proprio nome utente</li>
 *   <li>Specificare il nome della nuova stanza</li>
 *   <li>Impostare una password opzionale per la stanza</li>
 *   <li>Creare e unirsi alla stanza</li>
 * </ul>
 * <p>
 * La finestra gestisce in modo asincrono sia la creazione che la connessione
 * alla stanza, per mantenere l'interfaccia reattiva.
 * </p>
 */
public class CreateRoomDialog extends JDialog {
    /** Campo per l'inserimento del nome del giocatore. */
    private final JTextField playerNameField = new JTextField(20);
    
    /** Campo per l'inserimento del nome della stanza. */
    private final JTextField roomNameField = new JTextField(20);
    
    /** Campo per l'inserimento della password della stanza. */
    private final JPasswordField roomPasswordField = new JPasswordField(20);
    
    /** Pulsante per avviare la creazione della stanza. */
    private final JButton createButton = new JButton("Crea");

    /**
     * Crea una nuova finestra di dialogo per la creazione di una stanza.
     *
     * @param parent la finestra principale del gioco a cui questa finestra è associata
     */
    public CreateRoomDialog(GameWindow parent) {
        super(parent, "Crea una nuova stanza", true);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(25, 25, 25));

        Dimension fieldSize = new Dimension(200, 30);
        playerNameField.setPreferredSize(fieldSize);
        playerNameField.setMaximumSize(fieldSize);
        roomNameField.setPreferredSize(fieldSize);
        roomNameField.setMaximumSize(fieldSize);
        roomPasswordField.setPreferredSize(fieldSize);
        roomPasswordField.setMaximumSize(fieldSize);

        // Applica il limite di 30 caratteri
        setFieldLimit(playerNameField, 30);
        setFieldLimit(roomNameField, 30);
        setFieldLimit(roomPasswordField, 30);

        add(createFormPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);

        playerNameField.addActionListener(e -> roomNameField.requestFocus());
        roomNameField.addActionListener(e -> roomPasswordField.requestFocus());
        roomPasswordField.addActionListener(e -> createButton.doClick());
    }

    /**
     * Impone un limite sul numero di caratteri che possono essere inseriti in un campo di testo.
     * Utilizza un DocumentFilter per bloccare l'inserimento oltre il limite specificato.
     *
     * @param field il campo di testo da limitare
     * @param limit il numero massimo di caratteri consentiti
     */
    private void setFieldLimit(JTextField field, int limit) {
        PlainDocument doc = (PlainDocument) field.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (fb.getDocument().getLength() + string.length() <= limit) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (fb.getDocument().getLength() + text.length() - length <= limit) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    /**
     * Crea il pannello principale contenente i campi di input per i dati della stanza.
     * Include campi per nome giocatore, nome stanza e password opzionale.
     *
     * @return pannello configurato con tutti i campi di input
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(25, 25, 25));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Dati Stanza",
                TitledBorder.LEFT, TitledBorder.TOP, null, Color.LIGHT_GRAY
        ));

        panel.add(createField("Nome Giocatore:", playerNameField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createField("Nome Stanza:", roomNameField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createPasswordField("Password (opzionale):", roomPasswordField));

        return panel;
    }

    /**
     * Crea il pannello inferiore contenente il pulsante di creazione.
     *
     * @return pannello configurato con il pulsante di creazione
     */
    private JPanel createBottomPanel() {
        styleButton(createButton);
        createButton.addActionListener(e -> attemptRoomCreationAndJoin());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(new Color(25, 25, 25));
        panel.add(createButton);
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
     * Include un toggle button per mostrare/nascondere la password.
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
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(toggle, gbc);

        return panel;
    }

    /**
     * Applica lo stile visuale standard al pulsante.
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
     * Tenta di creare una nuova stanza e unirsi ad essa.
     * Verifica la validità dei campi, stabilisce la connessione al server se necessario
     * e gestisce il processo di creazione e accesso alla stanza in modo asincrono.
     */
    public void attemptRoomCreationAndJoin() {
        String playerName = playerNameField.getText().trim();
        String roomName = roomNameField.getText().trim();
        String roomPassword = new String(roomPasswordField.getPassword()).trim();

        if (playerName.isEmpty() || roomName.isEmpty()) {
            showError("Compila nome giocatore e nome stanza.");
            return;
        }

        createButton.setEnabled(false);
        createButton.setText("Creazione...");

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

                int socketPort = RestAccess.retrieveSocketPort(host, port);
                SocketClient socketClient = client.connect(host, socketPort);
                if (socketClient == null) {
                    showError("Connessione fallita");
                    return;
                }
            }

            client.createRoom(roomName, roomPassword).thenAccept(success -> {
                if (success) {
                    client.joinRoom(playerName, roomName, roomPassword).thenAccept(joinSuccess -> {
                        SwingUtilities.invokeLater(() -> {
                            if (joinSuccess) {
                                dispose();
                                window.showGamePanel();
                            } else {
                                resetButton("Errore nella connessione alla stanza");
                            }
                        });
                    });
                } else {
                    resetButton("Creazione stanza non riuscita");
                }
            });
        }).start();
    }

    /**
     * Resetta lo stato del pulsante di creazione e mostra un errore.
     *
     * @param error il messaggio di errore da visualizzare
     */
    private void resetButton(String error) {
        SwingUtilities.invokeLater(() -> {
            createButton.setEnabled(true);
            createButton.setText("Crea");
            showError(error);
        });
    }

    /**
     * Mostra un messaggio di errore in una finestra di dialogo modale.
     *
     * @param message il messaggio di errore da visualizzare
     */
    private void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "Errore", JOptionPane.ERROR_MESSAGE);
        });
    }
}