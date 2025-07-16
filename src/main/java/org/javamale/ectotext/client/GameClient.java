package org.javamale.ectotext.client;

import org.javamale.ectotext.client.gui.GameWindow;
import org.javamale.ectotext.client.network.SocketClient;
import org.javamale.ectotext.common.packet.ErrorCode;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.impl.*;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Client principale del gioco che implementa il pattern Singleton.
 * Questa classe gestisce:
 * <ul>
 *   <li>Comunicazione con il server (REST e Socket)</li>
 *   <li>Gestione dello stato del gioco</li>
 *   <li>Interfaccia utente tramite {@link GameWindow}</li>
 *   <li>Gestione delle stanze e dei giocatori</li>
 * </ul>
 */
public class GameClient {
    /** Istanza singleton della classe. */
    private static volatile GameClient instance;
    
    /** Lock per la sincronizzazione nella creazione del singleton. */
    private static final Object lock = new Object();

    /** Finestra principale del gioco. */
    private final GameWindow gameWindow;

    /** Client per la comunicazione socket con il server. */
    private SocketClient socketClient;
    
    /** Lock per la sincronizzazione delle operazioni sul socket. */
    private final Object socketClientLock = new Object();

    /** Indirizzo del server. */
    private String serverAddress = "localhost";

    /** Porta del servizio REST del server. */
    private int serverRestPort = 8080;
    
    /** Stato di salute del servizio REST. */
    private boolean restHealthy = false;

    /** Porta del servizio socket del server. */
    private int serverSocketPort = 6666;
    /**
     * Campo joinFuture di tipo {@code CompletableFuture<Boolean>}.
     */
    private CompletableFuture<Boolean> joinFuture;
    /**
     * Campo createFuture di tipo {@code CompletableFuture<Boolean>}.
     */
    private CompletableFuture<Boolean> createFuture;

    /** Nome della stanza corrente. */
    private String roomName;
    
    /** Nome del giocatore corrente. */
    private String playerName;

    /**
     * Costruttore privato per il pattern Singleton.
     * Inizializza la finestra principale del gioco.
     */
    private GameClient() {
        gameWindow = new GameWindow();
    }

    /**
     * Restituisce l'istanza singleton del client di gioco.
     * Thread-safe tramite doppio controllo del lock.
     *
     * @return l'istanza singleton di GameClient
     */
    public static GameClient getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new GameClient();
                }
            }
        }
        return instance;
    }

    /**
     * Restituisce la finestra di gioco.
     * @return valore di tipo GameWindow.
     */
    public GameWindow getGameWindow() {
        return gameWindow;
    }

    /**
     * Restituisce il client socket.
     * @return valore di tipo SocketClient.
     */
    public SocketClient getSocketClient() {
        return socketClient;
    }

    /**
     * Restituisce l'indirizzo del server.
     * @return valore di tipo String.
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * Imposta l'indirizzo del server.
     * @param serverAddress valore di tipo String.
     */
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    /**
     * Restituisce la porta REST del server.
     * @return valore di tipo int.
     */
    public int getServerRestPort() {
        return serverRestPort;
    }

    /**
     * Imposta la porta REST del server.
     * @param serverRestPort valore di tipo int.
     */
    public void setServerRestPort(int serverRestPort) {
        this.serverRestPort = serverRestPort;
    }

    /**
     * Indica se il servizio REST è attivo.
     * @return valore di tipo boolean.
     */
    public boolean isRestHealthy() {
        return restHealthy;
    }

    /**
     * Imposta lo stato di health REST.
     * @param restHealthy valore di tipo boolean.
     */
    public void setRestHealthy(boolean restHealthy) {
        this.restHealthy = restHealthy;
    }

    /**
     * Restituisce la porta socket del server.
     * @return valore di tipo int.
     */
    public int getServerSocketPort() {
        return serverSocketPort;
    }

    /**
     * Imposta la porta socket del server.
     * @param serverSocketPort valore di tipo int.
     */
    public void setServerSocketPort(int serverSocketPort) {
        this.serverSocketPort = serverSocketPort;
    }

    /**
     * Avvia la finestra del gioco.
     */
    public void startWindow() {
        gameWindow.setVisible(true);
    }

    /**
     * Stabilisce una connessione socket con il server.
     * <p>
     * Il metodo è thread-safe e gestisce la creazione di un nuovo {@link SocketClient}.
     * </p>
     *
     * @param host indirizzo del server
     * @param port porta del server socket
     * @return il client socket creato, o null in caso di errore
     * @throws IllegalStateException se il client è già connesso
     */
    public SocketClient connect(String host, int port) {
        synchronized (socketClientLock) {
            if (socketClient != null) {
                throw new IllegalStateException("Already connected");
            }

            try {
                socketClient = new SocketClient(host, port);
                socketClient.start();

                return socketClient;

            } catch (IOException e) {
                System.err.println(e.getMessage());

                return null;
            }
        }
    }

    /**
     * Disconnette il client dal server.
     */
    public void disconnect() {
        synchronized (socketClientLock) {
            if (socketClient == null) {
                return;
            }

            socketClient.close();
            socketClient = null;
        }
    }

    /**
     * Indica se il client è connesso.
     * @return valore di tipo boolean.
     */
    public boolean isConnected() {
        return socketClient != null;
    }

    /**
     * Richiede di entrare in una stanza di gioco.
     * <p>
     * La richiesta è asincrona e il risultato viene comunicato tramite
     * il {@link CompletableFuture} restituito.
     * </p>
     *
     * @param playerName nome del giocatore
     * @param roomName nome della stanza
     * @param roomPassword password della stanza (può essere vuota)
     * @return future che completerà con true se il join è riuscito, false altrimenti
     * @throws IllegalStateException se il client non è connesso
     */
    public CompletableFuture<Boolean> joinRoom(String playerName, String roomName, String roomPassword) {
        joinFuture = new CompletableFuture<>();

        socketClient.sendMessage(new JoinRoomPacket(playerName, roomName, roomPassword));

        return joinFuture;
    }

    /**
     * Completa la procedura di join alla stanza.
     * @param success valore di tipo boolean.
     */
    public void completeJoinRoom(boolean success) {
        if (joinFuture == null) {
            return;
        }
        joinFuture.complete(success);

        if (success) {
            socketClient.sendMessage(new GameGetAvailableCharactersPacket());
        }
    }

    /**
     * Invia una richiesta di creazione stanza.
     * @param roomName valore di tipo String.
     * @param roomPassword valore di tipo String.
     * @return valore di tipo {@code CompletableFuture<Boolean>}.
     */
    public CompletableFuture<Boolean> createRoom(String roomName, String roomPassword) {
        createFuture = new CompletableFuture<>();

        socketClient.sendMessage(new CreateRoomPacket(roomName, roomPassword));

        return createFuture;
    }

    /**
     * Completa la procedura di creazione stanza.
     * @param success valore di tipo boolean.
     */
    public void completeCreateRoom(boolean success) {
        if (createFuture == null) {
            return;
        }
        createFuture.complete(success);
    }

    /**
     * Invia un messaggio di chat.
     * @param message valore di tipo String.
     */
    public void sendChatMessage(String message) {
        socketClient.sendMessage(new ChatMessagePacket(playerName, message));
    }

    /**
     * Invia una richiesta di uscita dalla stanza.
     */
    public void disconnectRoom() {
        if (!isConnected()) {
            SwingUtilities.invokeLater(() -> gameWindow.showError("Non sei connesso al server."));
            return;
        }

        if (roomName == null || roomName.isEmpty()) {
            SwingUtilities.invokeLater(() -> gameWindow.showError("Non sei in nessuna stanza."));
            return;
        }

        socketClient.sendMessage(new DisconnectRoomPacket(roomName));
    }

    /**
     * Invia una richiesta di eliminazione stanza.
     */
    public void deleteRoom() {
        if (!isConnected()) {
            SwingUtilities.invokeLater(() -> gameWindow.showError("Non sei connesso al server."));
            return;
        }

        if (roomName == null || roomName.isEmpty()) {
            SwingUtilities.invokeLater(() -> gameWindow.showError("Non sei in nessuna stanza."));
            return;
        }

        socketClient.sendMessage(new DeleteRoomPacket(roomName));
    }

    /**
     * Seleziona un personaggio.
     * @param character valore di tipo String.
     */
    public void selectCharacter(String character) {
        socketClient.sendMessage(new GameSelectCharacterPacket(playerName, character));
    }

    /**
     * Invia un comando di gioco.
     * @param command valore di tipo String.
     */
    public void sendGameCommand(String command) {
        socketClient.sendMessage(new GameCommandPacket(playerName, command));
    }

    /**
     * Gestisce i pacchetti di aggiornamento ricevuti dal server.
     * Elabora i diversi tipi di pacchetti e aggiorna di conseguenza:
     * <ul>
     *   <li>Stato delle stanze</li>
     *   <li>Messaggi di chat</li>
     *   <li>Stato del gioco</li>
     *   <li>Errori e notifiche</li>
     * </ul>
     *
     * @param update il pacchetto ricevuto dal server
     * @see Packet
     */
    public void handleUpdate(Packet update){
        switch (update){
            case ErrorPacket errorPacket -> handleError(errorPacket);

            case RoomListPacket roomListPacket -> {
                SwingUtilities.invokeLater(()  -> gameWindow.receiveRoomList(roomListPacket.getRoomList()));
            }
            case RoomDetailsPacket roomDetailsPacket -> {
                SwingUtilities.invokeLater(() -> gameWindow.receiveRoomDetailsUpdate(roomDetailsPacket));
            }
            case RoomCreatedPacket ignored -> completeCreateRoom(true);
            case RoomJoinedPacket roomJoinedPacket -> {
                roomName = roomJoinedPacket.getRoomName();
                playerName = roomJoinedPacket.getPlayerName();
                completeJoinRoom(true);
            }
            case RoomDisconnectedPacket ignored -> {
                roomName = null;
                playerName = null;

                SwingUtilities.invokeLater(gameWindow::showLobbyPanel);
            }
            case RoomDeletedPacket roomDeletedPacket -> {
                SwingUtilities.invokeLater(() -> {
                    gameWindow.showInfo("Stanza cancellata",
                            "La stanza " + roomDeletedPacket.getRoomName() + " è stata cancellata"
                    );
                });
            }

            case ChatMessagePacket chatMessagePacket -> {
                SwingUtilities.invokeLater(() -> {
                    gameWindow.receiveChatMessage(chatMessagePacket.getPlayerName(), chatMessagePacket.getMessage());
                });
            }

            case GameAvailableCharactersPacket gameAvailableCharactersPacket -> {
                SwingUtilities.invokeLater(() -> {
                    gameWindow.receiveAvilableCharacters(gameAvailableCharactersPacket.getCharacters());
                });
            }
            case GameNarratorPacket gameNarratorPacket -> {
                SwingUtilities.invokeLater(() -> {
                    gameWindow.receiveNarratorMessage(gameNarratorPacket.getMessage());
                });
            }
            case GameDialoguePacket gameDialoguePacket -> {
                String speaker = gameDialoguePacket.getSpeaker();
                String message = gameDialoguePacket.getMessage();

                SwingUtilities.invokeLater(() -> {
                    gameWindow.receiveDialogueMessage(speaker, message);
                });
            }

            case null -> {
                System.err.println("Received null packet");
            }
            default -> {
                System.err.println("Unrecognized packet: \n" + update.toBaseJson());
            }
        }
    }

    /**
     * Gestisce i pacchetti di errore ricevuti dal server.
     * Gestisce diversi tipi di errori:
     * <ul>
     *   <li>Errori di gioco (codici 300-399)</li>
     *   <li>Errori di creazione stanza</li>
     *   <li>Errori di accesso alla stanza</li>
     *   <li>Errori di eliminazione stanza</li>
     * </ul>
     *
     * @param errorPacket il pacchetto di errore ricevuto
     * @see ErrorCode
     */
    public void handleError(ErrorPacket errorPacket) {
        if (errorPacket == null) {
            return;
        }

        if (errorPacket.getErrorCode().getCode() > 300 && errorPacket.getErrorCode().getCode() < 400){
            SwingUtilities.invokeLater(() -> {
                gameWindow.receiveErrorMessage(errorPacket.getErrorMessage());
            });
            return;
        }

        switch (errorPacket.getErrorCode()){
            case ROOM_NOT_CREATED, ROOM_ALREADY_EXISTS -> completeCreateRoom(false);
            case ROOM_NOT_FOUND -> completeJoinRoom(false);
            case FULL_ROOM -> {
                gameWindow.showError("Stanza piena");
                completeJoinRoom(false);
            }
            case NICKNAME_ALREADY_USED -> {
                gameWindow.showError("Nickname già utilizzato");
                completeJoinRoom(false);
            }
            case WRONG_ROOM_PASSWORD -> {
                gameWindow.showError("Password errata");
                completeJoinRoom(false);
            }
            case ROOM_NOT_DELETED -> gameWindow.showError("Stanza non eliminata correttamente");
            default -> {
                System.err.println("ERROR " + errorPacket.getErrorCode().getCode() + ": " + errorPacket.getErrorMessage());
            }
        }
    }

}