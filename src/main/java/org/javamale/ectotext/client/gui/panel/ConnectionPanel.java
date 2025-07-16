package org.javamale.ectotext.client.gui.panel;

import org.javamale.ectotext.client.GameClient;
import org.javamale.ectotext.client.gui.GameWindow;
import org.javamale.ectotext.client.rest.RestAccess;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.util.Arrays;

/**
 * Pannello di connessione che fornisce l'interfaccia utente per connettersi al server di gioco.
 * Questo pannello contiene:
 * <ul>
 *   <li>Campo per l'indirizzo del server</li>
 *   <li>Campo per la porta di connessione</li>
 *   <li>Pulsante per avviare la connessione</li>
 *   <li>Controllo per la musica di sottofondo</li>
 * </ul>
 * <p>
 * Il pannello presenta un'interfaccia grafica stilizzata con effetti di trasparenza
 * e blur, mantenendo il tema visivo del gioco Ghostbusters.
 * </p>
 */
public class ConnectionPanel extends JPanel {
    /** Campo di testo per l'inserimento dell'indirizzo del server. */
    private final JTextField serverAddressField;
    
    /** Campo di testo per l'inserimento della porta del server. */
    private final JTextField portField;
    
    /** Pulsante per avviare il tentativo di connessione. */
    private final JButton connectButton;
    
    /** Etichetta per il controllo dello stato della musica. */
    private final JLabel musicStatusLabel;
    
    /** Immagine di sfondo del pannello. */
    private final BufferedImage backgroundImage;
    
    /** Area rettangolare per l'effetto vetro smerigliato. */
    private Rectangle glassBounds = new Rectangle(0, 0, 400, 240);
    
    /** Riferimento alla finestra principale del gioco. */
    private final GameWindow gameWindow;

    /**
     * Crea un nuovo pannello di connessione.
     * Inizializza l'interfaccia grafica con effetti visivi e controlli per la connessione.
     *
     * @param gameWindow la finestra principale del gioco
     * @throws RuntimeException se l'immagine di sfondo non può essere caricata
     */
    public ConnectionPanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;

        try {
            backgroundImage = ImageIO.read(getClass().getResource("/img/background.jpg"));
        } catch (IOException | IllegalArgumentException e) {
            throw new RuntimeException("Immagine di sfondo non trovata: /img/background.jpg", e);
        }

        setLayout(new GridBagLayout());
        setOpaque(false);

        JPanel glassPanel = new JPanel();
        glassPanel.setOpaque(false);
        glassPanel.setLayout(new BoxLayout(glassPanel, BoxLayout.Y_AXIS));
        glassPanel.setBorder(new EmptyBorder(38, 38, 38, 38));
        glassPanel.setMaximumSize(new Dimension(1000, 600));

        JLabel title = new JLabel("👻 Incubo al Sedgewick Hotel", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 22));
        title.setForeground(new Color(255, 40, 40));
        title.setAlignmentX(CENTER_ALIGNMENT);

        glassPanel.add(title);
        glassPanel.add(Box.createVerticalStrut(28));

        musicStatusLabel = new JLabel("🔇", SwingConstants.CENTER);
        musicStatusLabel.setFont(new Font("Monospaced", Font.BOLD, 40));
        musicStatusLabel.setAlignmentX(RIGHT_ALIGNMENT);
        musicStatusLabel.setForeground(Color.WHITE);

        musicStatusLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                gameWindow.toggleMusic();
                updateMusicStatus();
            }
        });

        JPanel musicPanel = new JPanel();
        musicPanel.setOpaque(false);
        musicPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        musicPanel.add(musicStatusLabel);

        JPanel addressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        addressPanel.setOpaque(false);
        JLabel addressLabel = glassLabel("Indirizzo Server:");
        serverAddressField = glassField("localhost");
        addressPanel.add(addressLabel);
        addressPanel.add(serverAddressField);
        glassPanel.add(addressPanel);

        glassPanel.add(Box.createVerticalStrut(18));

        JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        portPanel.setOpaque(false);
        JLabel portLabel = glassLabel("Porta:");
        portField = glassField("8080");
        portPanel.add(portLabel);
        portPanel.add(portField);
        glassPanel.add(portPanel);

        glassPanel.add(Box.createVerticalStrut(22));

        connectButton = glassButton("Connetti");
        connectButton.setAlignmentX(CENTER_ALIGNMENT);
        connectButton.addActionListener(e -> attemptConnection());
        glassPanel.add(connectButton);

        glassPanel.add(musicPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.CENTER;
        add(glassPanel, gbc);

        glassPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) { updateGlassBounds(glassPanel); }
            public void componentMoved(java.awt.event.ComponentEvent evt) { updateGlassBounds(glassPanel); }
            public void componentShown(java.awt.event.ComponentEvent evt) { updateGlassBounds(glassPanel); }
        });

        updateMusicStatus();
    }

    /**
     * Aggiorna l'area dell'effetto vetro smerigliato in base alle dimensioni del pannello.
     *
     * @param glassPanel il pannello di cui aggiornare i bounds
     */
    private void updateGlassBounds(JPanel glassPanel) {
        Point p = SwingUtilities.convertPoint(glassPanel.getParent(), glassPanel.getLocation(), this);
        glassBounds = new Rectangle(p.x - 6, p.y - 6, glassPanel.getWidth() + 12, glassPanel.getHeight() + 12);
        repaint();
    }

    /**
     * Renderizza il pannello applicando l'effetto blur e gli effetti di trasparenza.
     *
     * @param g il contesto grafico su cui disegnare
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        if (glassBounds.width > 0 && glassBounds.height > 0) {
            BufferedImage zone = new BufferedImage(glassBounds.width, glassBounds.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2zone = zone.createGraphics();
            g2zone.drawImage(backgroundImage, -glassBounds.x, -glassBounds.y, getWidth(), getHeight(), this);
            g2zone.dispose();
            BufferedImage blurred = blurImage(zone, 22);  // More blur radius for smoother effect

            Graphics2D g2 = (Graphics2D) g.create();

            // Darker shadow behind the glass panel
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRoundRect(glassBounds.x + 6, glassBounds.y + 12, glassBounds.width - 12, glassBounds.height - 12, 40, 40);

            g2.setClip(new RoundRectangle2D.Float(glassBounds.x, glassBounds.y, glassBounds.width, glassBounds.height, 38, 38));
            g2.drawImage(blurred, glassBounds.x, glassBounds.y, this);

            // Slight shadow
            g2.setColor(new Color(28, 28, 28, 200));
            g2.fillRoundRect(glassBounds.x, glassBounds.y, glassBounds.width, glassBounds.height, 38, 38);

            // Red border for a 'Ghostbusters' vibe
            g2.setColor(new Color(255, 40, 40, 240));
            g2.setStroke(new BasicStroke(3.2f));
            g2.drawRoundRect(glassBounds.x, glassBounds.y, glassBounds.width - 1, glassBounds.height - 1, 38, 38);

            g2.dispose();
        }
    }

    /**
     * Applica un effetto di sfocatura gaussiana all'immagine specificata.
     *
     * @param image l'immagine da elaborare
     * @param blurRadius il raggio dell'effetto di sfocatura
     * @return l'immagine elaborata con l'effetto blur applicato
     */
    private static BufferedImage blurImage(BufferedImage image, int blurRadius) {
        float[] data = new float[blurRadius * blurRadius];
        float value = 1.0f / (blurRadius * blurRadius);
        Arrays.fill(data, value);
        Kernel kernel = new Kernel(blurRadius, blurRadius, data);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(image, null);
    }

    /**
     * Crea un'etichetta con stile visual consistente col tema del gioco.
     *
     * @param text il testo da visualizzare nell'etichetta
     * @return l'etichetta configurata con lo stile appropriato
     */
    private JLabel glassLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(230, 230, 230));
        l.setFont(new Font("SansSerif", Font.BOLD, 15));
        return l;
    }

    /**
     * Crea un campo di testo con effetto vetro e bordi arrotondati.
     *
     * @param defaultText il testo predefinito da mostrare nel campo
     * @return il campo di testo configurato con lo stile appropriato
     */
    private JTextField glassField(String defaultText) {
        JTextField f = new JTextField(defaultText, 13) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Shape s = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(38, 38, 38, 210));
                g2.fill(s);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(255, 40, 40, 110));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
                g2.dispose();
            }
        };
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(5, 15, 5, 15));
        f.setFont(new Font("SansSerif", Font.BOLD, 15));
        f.setForeground(Color.WHITE);
        f.setCaretColor(new Color(255, 40, 40));
        return f;
    }

    /**
     * Crea un pulsante con effetto vetro e stile Ghostbusters.
     *
     * @param text il testo da visualizzare sul pulsante
     * @return il pulsante configurato con lo stile appropriato
     */
    private JButton glassButton(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Shape s = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(new Color(80, 0, 0, 230));
                g2.fill(s);
                g2.setColor(new Color(255, 40, 40, 220));
                g2.setStroke(new BasicStroke(2.2f));
                g2.draw(s);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        b.setOpaque(false);
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 15));
        b.setBorder(new EmptyBorder(9, 26, 9, 26));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.setBackground(new Color(80, 0, 0));
        b.setForeground(Color.WHITE);

        UIManager.put("Button.disabledBackground", new Color(80, 0, 0));
        UIManager.put("Button.disabledText", new Color(150, 150, 150));

        return b;
    }

    /**
     * Avvia il tentativo di connessione al server.
     * Valida i dati inseriti e gestisce la comunicazione in modo asincrono.
     */
    private void attemptConnection() {
        GameClient gameClient = GameClient.getInstance();
        GameWindow gameWindow = gameClient.getGameWindow();

        String serverAddress = serverAddressField.getText().trim();
        String portText = portField.getText().trim();

        if (serverAddress.isEmpty() || portText.isEmpty()) {
            gameWindow.showError("Inserire sia l'indirizzo del server che la porta.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            gameWindow.showError("La porta deve essere un numero valido.");
            return;
        }

        connectButton.setEnabled(false);
        connectButton.setText("Comunicazione con il server...");

        new Thread(() -> {
            if (RestAccess.isHealthy(serverAddress, port)) {
                gameClient.setServerAddress(serverAddress);
                gameClient.setServerRestPort(port);
                gameClient.setRestHealthy(true);
                SwingUtilities.invokeLater(gameWindow::showLobbyPanel);
            } else {
                SwingUtilities.invokeLater(() -> {
                    gameWindow.showError("Connessione fallita");
                    connectButton.setText("Connetti");
                    connectButton.setEnabled(true);
                });
            }
        }).start();
    }

    /**
     * Aggiorna l'icona dello stato della musica in base allo stato corrente.
     * Alterna tra 🔊 (attiva) e 🔇 (disattiva).
     */
    public void updateMusicStatus() {
        if (gameWindow.getMusicPlaying()) {
            musicStatusLabel.setText("🔊");
        } else {
            musicStatusLabel.setText("🔇");
        }
    }
}