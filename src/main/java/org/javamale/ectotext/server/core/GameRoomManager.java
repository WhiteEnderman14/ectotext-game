package org.javamale.ectotext.server.core;

import org.javamale.ectotext.common.packet.ErrorCode;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.impl.*;
import org.javamale.ectotext.server.contracts.UpdateHandler;
import org.javamale.ectotext.server.network.ClientHandler;
import org.javamale.ectotext.server.persistence.DbManager;
import org.javamale.ectotext.server.persistence.dao.GameRoomDAO;
import org.javamale.ectotext.server.persistence.dao.impl.GameRoomDAOImpl;
import org.javamale.ectotext.server.util.PacketFactory;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestore centrale delle stanze di gioco del server.
 * Questa classe è responsabile di:
 * <ul>
 *   <li>Gestire il ciclo di vita delle stanze di gioco</li>
 *   <li>Coordinare la persistenza delle stanze su database</li>
 *   <li>Gestire le richieste di rete dei client non ancora in una stanza</li>
 *   <li>Implementare la logica di routing dei pacchetti di rete</li>
 * </ul>
 *
 * <p>
 * Il manager mantiene una mappatura thread-safe tra i nomi delle stanze
 * e le relative istanze {@link GameRoom}.
 * </p>
 *
 * @see GameRoom
 * @see UpdateHandler
 * @see GameRoomDAO
 */
public class GameRoomManager implements UpdateHandler {
    
    /**
     * Mappatura thread-safe tra nomi delle stanze e relative istanze.
     * <p>
     * La mappa viene sincronizzata esplicitamente per le operazioni di modifica
     * attraverso i metodi pubblici della classe.
     * </p>
     */
    private final Map<String, GameRoom> gameRooms;

    /**
     * Inizializza il manager e carica le stanze esistenti dal database.
     * Durante l'inizializzazione:
     * <ul>
     *   <li>Viene creata una nuova mappa vuota per le stanze</li>
     *   <li>Vengono caricate tutte le stanze dal database</li>
     *   <li>Viene ripristinato lo stato di gioco di ogni stanza</li>
     * </ul>
     *
     * @throws RuntimeException se si verificano errori critici nell'accesso al database
     */
    public GameRoomManager() {
        this.gameRooms = new HashMap<>();
        GameRoomDAO gameRoomDAO = new GameRoomDAOImpl(DbManager.getConnection());

        try {
            for (GameRoom room : gameRoomDAO.getAll()) {
                room.retrieveGameState();
                gameRooms.put(room.getName(), room);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Ottiene tutte le stanze di gioco attualmente attive.
     *
     * @return collezione immutabile delle stanze attive
     */
    public Collection<GameRoom> getGameRooms() {
        return gameRooms.values();
    }

    /**
     * Cerca una stanza di gioco per nome.
     *
     * @param name nome univoco della stanza
     * @return la stanza corrispondente, o {@code null} se non esistente
     */
    public GameRoom getGameRoom(String name) {
        return gameRooms.get(name);
    }

    /**
     * Crea una nuova stanza di gioco.
     * La creazione include:
     * <ul>
     *   <li>Verifica dell'unicità del nome</li>
     *   <li>Persistenza della stanza su database</li>
     *   <li>Aggiunta della stanza alla mappatura in memoria</li>
     * </ul>
     *
     * @param roomName nome univoco della nuova stanza
     * @param roomPassword password di accesso alla stanza
     * @return {@code true} se la stanza è stata creata, {@code false} se esiste già o si sono verificati errori
     */
    public boolean createGameRoom(String roomName, String roomPassword) {
        if (gameRooms.containsKey(roomName)) {
            return false;
        }

        GameRoomDAO gameRoomDAO = new GameRoomDAOImpl(DbManager.getConnection());

        GameRoom gameRoom = new GameRoom(roomName, roomPassword);

        try {
            gameRoomDAO.add(gameRoom);
            gameRooms.put(roomName, gameRoom);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Rimuove una stanza di gioco esistente.
     * La rimozione include:
     * <ul>
     *   <li>Eliminazione della stanza dal database</li>
     *   <li>Rimozione della stanza dalla mappatura in memoria</li>
     * </ul>
     *
     * @param roomName nome della stanza da rimuovere
     * @return {@code true} se la stanza è stata rimossa, {@code false} se non esisteva o si sono verificati errori
     */
    public boolean removeGameRoom(String roomName) {
        if (!gameRooms.containsKey(roomName)) {
            return false;
        }

        GameRoomDAO gameRoomDAO = new GameRoomDAOImpl(DbManager.getConnection());

        try {
            gameRoomDAO.delete(roomName);
            gameRooms.remove(roomName);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Gestisce i pacchetti di rete ricevuti dai client non ancora in una stanza.
     * Gestisce le seguenti operazioni:
     * <ul>
     *   <li>Creazione di nuove stanze</li>
     *   <li>Ingresso dei giocatori nelle stanze</li>
     *   <li>Richieste di informazioni sulle stanze</li>
     *   <li>Errori per operazioni non consentite fuori dalle stanze</li>
     * </ul>
     *
     * @param client client che ha inviato il pacchetto
     * @param update pacchetto ricevuto
     * @throws NullPointerException se client o update sono null
     * @see Packet
     * @see ErrorCode
     */
    @Override
    public synchronized void onUpdate(ClientHandler client, Packet update) {
        switch (update) {
            case JoinRoomPacket joinRoomPacket -> {
                String playerName = joinRoomPacket.getPlayerName();
                String roomName = joinRoomPacket.getRoomName();
                String roomPassword = joinRoomPacket.getRoomPassword();

                GameRoom gameRoom = gameRooms.get(roomName);
                if (gameRoom == null) {
                    client.sendMessage(new ErrorPacket(ErrorCode.ROOM_NOT_FOUND));
                    return;
                }

                if (!gameRoom.checkPassword(roomPassword)) {
                    client.sendMessage(new ErrorPacket(ErrorCode.WRONG_ROOM_PASSWORD));
                    return;
                }

                if(gameRoom.getPlayer(playerName) != null) {
                    client.sendMessage(new ErrorPacket(ErrorCode.NICKNAME_ALREADY_USED));
                    return;
                }

                if (!gameRoom.addPlayer(playerName, client)) {
                    client.sendMessage(new ErrorPacket(ErrorCode.FULL_ROOM));
                    return;
                }

                client.setUpdateHandler(gameRoom);
                client.sendMessage(new RoomJoinedPacket(playerName, roomName));
                gameRoom.broadcastPacket(new RoomDetailsPacket(gameRoom.getName(), gameRoom.getPlayerNames()));
                gameRoom.getIntro().forEach(client::sendMessage);
            }
            case CreateRoomPacket createRoomPacket -> {
                String roomName = createRoomPacket.getRoomName();
                String roomPassword = createRoomPacket.getRoomPassword();

                if(gameRooms.containsKey(roomName)) {
                    client.sendMessage(new ErrorPacket(ErrorCode.ROOM_ALREADY_EXISTS));
                    return;
                }

                if(!createGameRoom(roomName, roomPassword)) {
                    client.sendMessage(new ErrorPacket(ErrorCode.ROOM_NOT_CREATED));
                    return;
                }

                client.sendMessage(new RoomCreatedPacket(roomName));
            }
            case GetRoomDetailsPacket roomDetailsPacket -> {
                String roomName = roomDetailsPacket.getRoomName();

                GameRoom gameRoom = gameRooms.get(roomName);

                if (gameRoom == null) {
                    client.sendMessage(new ErrorPacket(ErrorCode.ROOM_NOT_FOUND));
                    return;
                }

                client.sendMessage(new RoomDetailsPacket(gameRoom.getName(), gameRoom.getPlayerNames()));
            }
            case GetRoomListPacket ignored -> client.sendMessage(PacketFactory.fromGameRoomList(gameRooms.values()));

            case DeleteRoomPacket ignored -> client.sendMessage(new ErrorPacket(ErrorCode.NOT_IN_ROOM));
            case DisconnectRoomPacket ignored -> client.sendMessage(new ErrorPacket(ErrorCode.NOT_IN_ROOM));

            case ChatMessagePacket ignored -> client.sendMessage(new ErrorPacket(ErrorCode.NOT_IN_ROOM));

            case GameCommandPacket ignored -> client.sendMessage(new ErrorPacket(ErrorCode.NOT_IN_ROOM));
            case GameSelectCharacterPacket ignored -> client.sendMessage(new ErrorPacket(ErrorCode.NOT_IN_ROOM));
            case GameGetAvailableCharactersPacket ignored -> client.sendMessage(new ErrorPacket(ErrorCode.NOT_IN_ROOM));

            case null -> client.sendMessage(new ErrorPacket(ErrorCode.INVALID_PACKET));
            default -> client.sendMessage(new ErrorPacket(ErrorCode.UNRECOGNIZED_PACKET));
        }
    }

    /**
     * Gestisce la disconnessione di un client dal manager.
     * <p>
     * Chiude la connessione con il client quando questo non è
     * ancora entrato in nessuna stanza.
     * </p>
     *
     * @param client client disconnesso
     * @throws NullPointerException se client è null
     */
    @Override
    public void onDisconnect(ClientHandler client) {
        client.close();
    }
}