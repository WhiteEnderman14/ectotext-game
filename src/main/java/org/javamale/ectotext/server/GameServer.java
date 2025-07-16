package org.javamale.ectotext.server;

import com.google.gson.stream.JsonReader;
import org.javamale.ectotext.server.core.GameRoomManager;
import org.javamale.ectotext.server.network.SocketServer;
import org.javamale.ectotext.server.rest.RestServer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Server principale del gioco che gestisce tutti i servizi necessari.
 * Questa classe implementa il pattern fornisce:
 * <ul>
 *   <li>Inizializzazione coordinata di tutti i servizi del server</li>
 *   <li>Gestione delle stanze di gioco</li>
 *   <li>Comunicazione socket per il gioco in tempo reale</li>
 *   <li>API REST per informazioni di stato</li>
 *   <li>Caricamento delle configurazioni delle stanze</li>
 * </ul>
 */
public class GameServer {
    /** Gestore delle stanze di gioco. */
    private static GameRoomManager roomManager;
    
    /** Server per la comunicazione in tempo reale via socket. */
    private static SocketServer socketServer;
    
    /** Server REST per le API di stato e configurazione. */
    private static RestServer restServer;

    /**
     * Inizializza tutti i servizi del server con porte personalizzate.
     * Sequenza di inizializzazione:
     * <ol>
     *   <li>Creazione del gestore delle stanze</li>
     *   <li>Avvio del server socket sulla porta specificata</li>
     *   <li>Avvio del server REST sulla porta specificata</li>
     * </ol>
     *
     * @param socketPort porta per il server socket
     * @param restPort porta per il server REST
     * @throws IOException se si verificano errori durante l'inizializzazione dei server
     */
    public static void init(int socketPort, int restPort) throws IOException {
        roomManager = new GameRoomManager();
        socketServer = new SocketServer(socketPort);
        socketServer.start();
        restServer = new RestServer(restPort);
        restServer.start();
    }

    /**
     * Inizializza tutti i servizi del server con le porte predefinite.
     * Utilizza:
     * <ul>
     *   <li>Porta 6666 per il server socket</li>
     *   <li>Porta 8080 per il server REST</li>
     * </ul>
     *
     * @throws IOException se si verificano errori durante l'inizializzazione dei server
     * @see #init(int, int)
     */
    public static void init() throws IOException {
        init(6666, 8080);
    }

    /**
     * Arresta in modo ordinato tutti i servizi del server.
     * L'ordine di arresto è:
     * <ol>
     *   <li>Server socket (chiude le connessioni attive)</li>
     *   <li>Server REST (termina le richieste in corso)</li>
     * </ol>
     */
    public static void shutdown() {
        socketServer.shutdown();
        restServer.shutdown();
    }

    /**
     * Restituisce il gestore delle stanze di gioco.
     *
     * @return l'istanza del GameRoomManager
     */
    public static GameRoomManager getRoomManager() {
        return roomManager;
    }

    /**
     * Restituisce il server socket per la comunicazione in tempo reale.
     *
     * @return l'istanza del SocketServer
     */
    public static SocketServer getSocketServer() {
        return socketServer;
    }

    /**
     * Restituisce il server REST per le API di stato.
     *
     * @return l'istanza del RestServer
     */
    public static RestServer getRestServer() {
        return restServer;
    }

    /**
     * Restituisce la porta su cui è in ascolto il server socket.
     *
     * @return il numero di porta del server socket
     */
    public static int getSocketPort() {
        return socketServer.getPort();
    }

    /**
     * Restituisce la porta su cui è in ascolto il server REST.
     *
     * @return il numero di porta del server REST
     */
    public static int getRestPort() {
        return restServer.getPort();
    }

    /**
     * Carica le configurazioni delle stanze di gioco da un file JSON.
     * Il file JSON deve contenere un array di oggetti con i seguenti campi:
     * <ul>
     *   <li>{@code room_name}: nome univoco della stanza</li>
     *   <li>{@code room_password}: password opzionale per l'accesso</li>
     * </ul>
     *
     * @param jsonFile file JSON contenente le configurazioni delle stanze
     * @return numero di stanze caricate con successo
     * @throws IOException se il file non è leggibile, il formato JSON non è valido
     *                     o una stanza con lo stesso nome esiste già
     */
    public static int loadGameRoomsFromJson(File jsonFile) throws IOException {
        int roomCount = 0;

        try (JsonReader reader = new JsonReader(new FileReader(jsonFile))) {
            reader.beginArray();
            while (reader.hasNext()) {
                String roomName = null;
                String roomPassword = null;

                reader.beginObject();
                while (reader.hasNext()) {
                    String fieldName = reader.nextName();
                    switch (fieldName) {
                        case "room_name" -> roomName = reader.nextString();
                        case "room_password" -> roomPassword = reader.nextString();
                        default -> reader.skipValue();
                    }
                }
                reader.endObject();

                if(getRoomManager().createGameRoom(roomName, roomPassword)) {
                    roomCount++;
                } else {
                    throw new IOException("Room " + roomName + " already exists");
                }
            }
            reader.endArray();
        }

        return roomCount;
    }

}