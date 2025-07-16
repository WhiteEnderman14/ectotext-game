package org.javamale.ectotext.client.gui;

import org.javamale.ectotext.client.GameClient;
import org.javamale.ectotext.client.gui.panel.ConnectionPanel;
import org.javamale.ectotext.client.gui.panel.LobbyPanel;
import org.javamale.ectotext.client.gui.panel.GamePlayPanel;
import org.javamale.ectotext.common.packet.impl.RoomDetailsPacket;
import org.javamale.ectotext.common.packet.impl.RoomListPacket;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * Finestra principale del gioco che gestisce l'interfaccia utente e la musica di sottofondo.
 * Questa classe gestisce:
 * <ul>
 *   <li>Navigazione tra i diversi pannelli del gioco (connessione, lobby, gioco)</li>
 *   <li>Riproduzione e controllo della musica di sottofondo</li>
 *   <li>Gestione dei messaggi di sistema e di gioco</li>
 *   <li>Comunicazione con il server di gioco</li>
 * </ul>
 */
public class GameWindow extends JFrame {
    /** Clip audio per la riproduzione della musica di sottofondo. */
    private Clip backgroundMusicClip;
    
    /** Flag che indica se la musica è attualmente in riproduzione. */
    private boolean isMusicPlaying = false;

    /** Pannello per la gestione della connessione al server. */
    private final ConnectionPanel connectionPanel;
    
    /** Pannello per la visualizzazione e gestione della lobby. */
    private final LobbyPanel lobbyPanel;
    
    /** Pannello principale di gioco. */
    private final GamePlayPanel gamePlayPanel;

    /**
     * Crea e inizializza la finestra principale del gioco.
     * Configura:
     * <ul>
     *   <li>Dimensioni e proprietà della finestra</li>
     *   <li>Pannelli di gioco (connessione, lobby, gameplay)</li>
     *   <li>Stili dell'interfaccia utente</li>
     *   <li>Gestione della chiusura della finestra</li>
     *   <li>Musica di sottofondo</li>
     * </ul>
     */
    public GameWindow() {
        setTitle("Incubo al Sedgewick Hotel - Game Client");
        super.setIconImage(new ImageIcon(getClass().getResource("/img/favicon.png")).getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));
        setResizable(false);

        // Creazione dei pannelli
        connectionPanel = new ConnectionPanel(this); // Passa GameWindow come parametro
        lobbyPanel = new LobbyPanel(this); // Passa GameWindow come parametro
        gamePlayPanel = new GamePlayPanel(this);

        // Impostazioni di font generali
        UIManager.put("Label.font", new Font("Arial", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("Arial", Font.BOLD, 14));
        UIManager.put("TextField.font", new Font("Arial", Font.PLAIN, 14));
        UIManager.put("TextArea.font", new Font("Courier New", Font.PLAIN, 14));
        UIManager.put("List.font", new Font("Arial", Font.PLAIN, 14));

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                GameClient.getInstance().disconnect();
                dispose();
                stopBackgroundMusic();  // Stop music when the window is closed
                System.exit(0);
            }
        });

        showConnectionPanel();
        playBackgroundMusic();  // Start playing the music as soon as the game window is created
    }

    /**
     * Avvia la riproduzione della musica di sottofondo.
     * <p>
     * Se la musica è già in riproduzione, l'operazione viene ignorata.
     * In caso di errore durante il caricamento, viene registrato un messaggio
     * di errore ma il gioco continua a funzionare.
     * </p>
     */
    public void playBackgroundMusic() {
        if (isMusicPlaying) {
            System.out.println("Musica già in esecuzione");
            return;  // La musica è già in esecuzione, non fare nulla
        }

        try {
            // Ferma qualsiasi musica già in esecuzione
            stopBackgroundMusic();

            // Carica il nuovo file audio
            InputStream musicStream = getClass().getResourceAsStream("/music/Ghostbusters.wav");
            if (musicStream != null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(musicStream));
                backgroundMusicClip = AudioSystem.getClip();
                backgroundMusicClip.open(audioStream);
                backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);  // Riproduce in loop
                setMusicPlaying(true);
            } else {
                System.err.println("Musica non trovata!");
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            System.err.println("Errore nel caricamento della musica");
        }
    }

    /**
     * Interrompe la riproduzione della musica di sottofondo.
     * L'operazione viene eseguita solo se la musica è effettivamente in riproduzione.
     */
    public void stopBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();  // Ferma la musica
            setMusicPlaying(false);
        }
    }

    /**
     * Alterna lo stato della musica tra riproduzione e pausa.
     */
    public void toggleMusic() {
        if (isMusicPlaying) {
            stopBackgroundMusic();
        } else {
            playBackgroundMusic();
        }
    }

    /**
     * Visualizza il pannello di connessione e imposta il titolo appropriato.
     */
    public void showConnectionPanel() {
        setResizable(false);
        setTitle("Incubo al Sedgewick Hotel - Connessione");

        switchPanel(connectionPanel);
    }

    /**
     * Visualizza il pannello della lobby e aggiorna la lista delle stanze.
     */
    public void showLobbyPanel() {
        setResizable(true);
        setMinimumSize(new Dimension(900, 600));
        setTitle("Incubo al Sedgewick Hotel - Lobby");

        switchPanel(lobbyPanel);
        lobbyPanel.attemptRoomListRefresh();
    }

    /**
     * Visualizza il pannello di gioco e pulisce le chat precedenti.
     */
    public void showGamePanel() {
        setResizable(true);
        setMinimumSize(new Dimension(900, 600));
        setTitle("Incubo al Sedgewick Hotel - In Gioco");

        gamePlayPanel.clearAllChat();
        switchPanel(gamePlayPanel);
    }

    /**
     * Sostituisce il pannello correntemente visualizzato con quello specificato.
     *
     * @param panel il nuovo pannello da visualizzare
     */
    private void switchPanel(JPanel panel) {
        getContentPane().removeAll();
        getContentPane().add(panel);
        revalidate();
        repaint();
    }

    /**
     * Mostra una finestra di dialogo con un messaggio di errore.
     *
     * @param message il messaggio di errore da visualizzare
     */
    public void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    message,
                    "Errore",
                    JOptionPane.ERROR_MESSAGE);
        });
    }

    /**
     * Mostra una finestra di dialogo con un messaggio informativo.
     *
     * @param title il titolo della finestra di dialogo
     * @param message il messaggio da visualizzare
     */
    public void showInfo(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    this,
                    message,
                    title,
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    /**
     * Aggiorna la lista delle stanze disponibili nella lobby.
     *
     * @param rooms collezione delle stanze disponibili
     * @throws NullPointerException se rooms è null
     */
    public void receiveRoomList(Collection<RoomListPacket.RoomListEntry> rooms) {
        lobbyPanel.populateRoomList(rooms);
    }

    /**
     * Gestisce la ricezione di un messaggio di chat.
     *
     * @param sender il nome del mittente del messaggio
     * @param message il contenuto del messaggio
     */
    public void receiveChatMessage(String sender, String message) {
        gamePlayPanel.receiveChatMessage(sender, message);
    }

    /**
     * Aggiorna i dettagli della stanza corrente.
     *
     * @param roomDetailsPacket pacchetto contenente i dettagli aggiornati della stanza
     */
    public void receiveRoomDetailsUpdate(RoomDetailsPacket roomDetailsPacket) {
        gamePlayPanel.receiveUserList(roomDetailsPacket.getUsers());
        setTitle("Incubo al Sedgewick Hotel - In Gioco - Stanza: " + roomDetailsPacket.getRoomName());
    }

    /**
     * Aggiorna la lista dei personaggi disponibili per la selezione.
     *
     * @param characters collezione dei personaggi disponibili
     */
    public void receiveAvilableCharacters(Collection<String> characters) {
        gamePlayPanel.setCharacterButtons(characters);
    }

    /**
     * Visualizza un messaggio del narratore nel pannello di gioco.
     *
     * @param message il messaggio del narratore da visualizzare
     */
    public void receiveNarratorMessage(String message) {
        gamePlayPanel.receiveNarratorMessage(message);
    }

    /**
     * Visualizza un dialogo nel pannello di gioco.
     *
     * @param speaker il nome del personaggio che parla
     * @param message il contenuto del dialogo
     */
    public void receiveDialogueMessage(String speaker, String message) {
        gamePlayPanel.receiveDialogueMessage(speaker, message);
    }

    /**
     * Visualizza un messaggio di errore nel pannello di gioco.
     *
     * @param message il messaggio di errore da visualizzare
     */
    public void receiveErrorMessage(String message) {
        gamePlayPanel.receiveErrorMessage(message);
    }

    /**
     * Verifica se la musica di sottofondo è attualmente in riproduzione.
     *
     * @return true se la musica è in riproduzione, false altrimenti
     */
    public boolean getMusicPlaying() {
        return isMusicPlaying;
    }

    /**
     * Imposta lo stato della musica e notifica i pannelli del cambiamento.
     *
     * @param isMusicPlaying true per indicare che la musica è in riproduzione,
     *                       false altrimenti
     */
    public void setMusicPlaying(boolean isMusicPlaying) {
        this.isMusicPlaying = isMusicPlaying;
        // Notifica i pannelli che lo stato della musica è cambiato
        connectionPanel.updateMusicStatus();
        lobbyPanel.updateMusicStatus();
        gamePlayPanel.updateMusicStatus();
    }
}