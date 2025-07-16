package org.javamale.ectotext.client.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Finestra di dialogo modale per la visualizzazione della mappa del gioco.
 * Permette di visualizzare e navigare tra:
 * <ul>
 *   <li>La mappa del piano terra</li>
 *   <li>La mappa del dodicesimo piano</li>
 * </ul>
 * Include controlli per passare da un piano all'altro e visualizzazione
 * scalabile delle mappe.
 */
public class MapDialog extends JDialog {
    /** Etichetta per la visualizzazione dell'immagine della mappa. */
    private final JLabel imageLabel;
    
    /** Pulsante per visualizzare la mappa del piano terra. */
    private final JButton groundButton;
    
    /** Pulsante per visualizzare la mappa del dodicesimo piano. */
    private final JButton twelfthButton;

    /** Percorso della risorsa immagine per la mappa del piano terra. */
    private final String groundFloorPath = "img/piano_terra.png";
    
    /** Percorso della risorsa immagine per la mappa del dodicesimo piano. */
    private final String twelfthFloorPath = "img/piano_12.png";

    /**
     * Crea una nuova finestra di dialogo per la visualizzazione della mappa.
     * Inizializza l'interfaccia con:
     * <ul>
     *   <li>Pulsanti per la selezione del piano</li>
     *   <li>Area di visualizzazione della mappa con scroll</li>
     *   <li>Dimensionamento automatico delle immagini</li>
     * </ul>
     *
     * @param owner la finestra proprietaria del dialogo
     */
    public MapDialog(Window owner) {
        super(owner, "Mappa", ModalityType.APPLICATION_MODAL);

        setSize(800, 500);
        setLocationRelativeTo(owner);

        setResizable(false);

        getContentPane().setBackground(new Color(30, 30, 30));
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(30, 30, 30));

        groundButton = createMapButton("Piano Terra");
        twelfthButton = createMapButton("12° Piano");

        groundButton.addActionListener(e -> switchToFloor(true));
        twelfthButton.addActionListener(e -> switchToFloor(false));

        buttonPanel.add(groundButton);
        buttonPanel.add(twelfthButton);

        add(buttonPanel, BorderLayout.NORTH);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        add(scrollPane, BorderLayout.CENTER);

        switchToFloor(true);
    }

    /**
     * Crea un pulsante stilizzato per la selezione del piano.
     * <p>
     * Applica uno stile visuale con:
     * <ul>
     *   <li>Sfondo verde</li>
     *   <li>Testo bianco in grassetto</li>
     *   <li>Bordi arrotondati</li>
     *   <li>Feedback visuale al passaggio del mouse</li>
     * </ul>
     * </p>
     *
     * @param text il testo da visualizzare sul pulsante
     * @return il pulsante configurato con lo stile appropriato
     */
    private JButton createMapButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(0, 100, 0)); // Verde per il pulsante
        b.setForeground(Color.WHITE); // Testo bianco
        b.setFont(new Font("SansSerif", Font.BOLD, 12)); // Font più piccolo
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(100, 30)); // Dimensioni più piccole
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 0))); // Bordo verde
        return b;
    }

    /**
     * Cambia la visualizzazione tra piano terra e dodicesimo piano.
     * <p>
     * Aggiorna sia l'immagine visualizzata che lo stato dei pulsanti,
     * disabilitando il pulsante del piano correntemente visualizzato.
     * </p>
     *
     * @param ground true per visualizzare il piano terra, false per il dodicesimo piano
     */
    private void switchToFloor(boolean ground) {
        String path = ground ? groundFloorPath : twelfthFloorPath;
        loadImage(path);

        // Stato bottoni
        groundButton.setEnabled(!ground);
        groundButton.setBackground(ground ? new Color(60, 60, 60) : new Color(0, 100, 0));
        twelfthButton.setEnabled(ground);
        twelfthButton.setBackground(!ground ? new Color(60, 60, 60) : new Color(0, 100, 0));
    }

    /**
     * Carica e ridimensiona l'immagine della mappa specificata.
     * <p>
     * L'immagine viene:
     * <ul>
     *   <li>Caricata dalle risorse dell'applicazione</li>
     *   <li>Ridimensionata mantenendo le proporzioni</li>
     *   <li>Limitata alle dimensioni della finestra</li>
     * </ul>
     * In caso di errore nel caricamento, viene mostrato un messaggio appropriato.
     * </p>
     *
     * @param path il percorso della risorsa immagine da caricare
     */
    private void loadImage(String path) {
        try {
            ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource(path));
            if (icon.getIconWidth() <= 0) throw new Exception("File non trovato: " + path);

            int maxWidth = getWidth() - 80;
            int maxHeight = getHeight() - 100;

            Image img = icon.getImage();
            int imgW = icon.getIconWidth();
            int imgH = icon.getIconHeight();

            double scaleW = (double) maxWidth / imgW;
            double scaleH = (double) maxHeight / imgH;
            double scale = Math.min(scaleW, scaleH);
            scale = Math.min(scale, 1.0);

            int newW = (int) (imgW * scale);
            int newH = (int) (imgH * scale);

            Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Errore nel caricamento dell'immagine della mappa.", "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }
}