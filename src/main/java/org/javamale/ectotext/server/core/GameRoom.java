package org.javamale.ectotext.server.core;

import org.javamale.ectotext.common.model.CommandHandler;
import org.javamale.ectotext.common.model.GameState;
import org.javamale.ectotext.common.model.impl.EctoTextCreator;
import org.javamale.ectotext.common.packet.ErrorCode;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.impl.*;
import org.javamale.ectotext.server.GameServer;
import org.javamale.ectotext.server.contracts.UpdateHandler;
import org.javamale.ectotext.server.network.ClientHandler;
import org.javamale.ectotext.server.persistence.DbManager;
import org.javamale.ectotext.server.persistence.dao.GameStateDAO;
import org.javamale.ectotext.server.util.DAOFactory;
import org.javamale.ectotext.server.util.PacketFactory;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rappresenta una stanza di gioco multigiocatore con gestione dello stato e delle comunicazioni.
 * Questa classe implementa:
 * <ul>
 *   <li>Gestione dei giocatori connessi (max {@value #MAX_PLAYERS})</li>
 *   <li>Protezione della stanza tramite password</li>
 *   <li>Comunicazione in tempo reale tra i giocatori</li>
 *   <li>Persistenza dello stato di gioco</li>
 *   <li>Gestione del ciclo di vita dei personaggi</li>
 * </ul>
 *
 * @see UpdateHandler
 * @see GameEngine
 * @see ClientHandler
 */
public class GameRoom implements UpdateHandler {
    /** 
     * Limite massimo di giocatori ammessi nella stanza.
     */
    public static final int MAX_PLAYERS = 3;

    /** 
     * Identificatore univoco della stanza.
     */
    private final String name;

    /** 
     * Chiave di accesso per proteggere la stanza.
     */
    private final String password;

    /** 
     * Registro thread-safe dei giocatori attivi.
     * La chiave è il nickname del giocatore, il valore è il suo handler di comunicazione.
     */
    private final Map<String, ClientHandler> players;

    /** 
     * Motore che gestisce la logica di gioco della stanza.
     * @see GameEngine
     */
    private GameEngine gameEngine;

    /**
     * Crea una nuova stanza di gioco.
     * Inizializza:
     * <ul>
     *   <li>Registro dei giocatori thread-safe</li>
     *   <li>Motore di gioco con regole predefinite</li>
     * </ul>
     *
     * @param name identificatore univoco della stanza
     * @param password chiave di accesso alla stanza
     */
    public GameRoom(String name, String password) {
        this.name = name;
        this.password = password;
        this.players = new ConcurrentHashMap<>(3);
        gameEngine = new GameEngine(new EctoTextCreator());
    }

    /**
     * Ottiene l'identificatore della stanza.
     *
     * @return nome univoco della stanza
     */
    public String getName() {
        return name;
    }

    /**
     * Ottiene la password della stanza.
     *
     * @return chiave di accesso alla stanza
     */
    public String getPassword() {
        return password;
    }

    /**
     * Conta i giocatori attualmente connessi.
     *
     * @return numero di giocatori presenti
     */
    public int getPlayerCount() {
        return players.size();
    }

    /**
     * Elenca i nickname dei giocatori connessi.
     *
     * @return collezione immutabile dei nickname
     */
    public Collection<String> getPlayerNames() {
        return players.keySet();
    }

    /**
     * Elenca gli handler di tutti i client connessi.
     *
     * @return collezione immutabile degli handler
     */
    public Collection<ClientHandler> getPlayers() {
        return players.values();
    }

    /**
     * Cerca l'handler di un giocatore specifico.
     *
     * @param name nickname del giocatore
     * @return handler del client, o {@code null} se non trovato
     * @throws IllegalArgumentException se name è null o vuoto
     */
    public ClientHandler getPlayer(String name) {
        return players.get(name);
    }

    /**
     * Recupera i messaggi introduttivi della stanza.
     *
     * @return lista ordinata dei pacchetti di introduzione
     * @see Packet
     */
    public List<Packet> getIntro() {
        return gameEngine.getIntroPackets();
    }

    /**
     * Ripristina lo stato del gioco dal database.
     * <p>
     * Se non esiste uno stato salvato, la stanza rimane nello stato iniziale.
     * </p>
     *
     * @throws SQLException in caso di errori di accesso al database
     */
    public void retrieveGameState() throws SQLException {
        GameStateDAO gameStateDAO = DAOFactory.createGameStateDAO(DbManager.getConnection(), gameEngine.getGameDescription());
        GameState gameState = gameStateDAO.get(name);

        if (gameState == null) {
            return;
        }

        gameEngine.setGameState(gameState);
        gameEngine.setIntro(false);
    }

    /**
     * Verifica la validità di una password.
     *
     * @param password password da verificare
     * @return {@code true} se la password è corretta
     */
    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    /**
     * Aggiunge un nuovo giocatore alla stanza.
     * L'operazione fallisce se:
     * <ul>
     *   <li>La stanza è piena ({@value #MAX_PLAYERS} giocatori)</li>
     *   <li>Il nickname è già in uso</li>
     * </ul>
     *
     * @param playerName nickname del nuovo giocatore
     * @param client handler di comunicazione del client
     * @return {@code true} se l'aggiunta ha successo
     */
    public synchronized boolean addPlayer(String playerName, ClientHandler client) {
        if (players.size() < MAX_PLAYERS && !players.containsKey(playerName)) {
            players.put(playerName, client);
            return true;
        }
        return false;
    }

    /**
     * Cerca il nickname associato a un client.
     *
     * @param client handler del client
     * @return nickname del giocatore, o {@code null} se non trovato
     */
    public String findPlayerName(ClientHandler client) {
        return players.entrySet().stream()
                .filter(e -> e.getValue().equals(client))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * Rimuove un giocatore e chiude la sua connessione.
     *
     * @param playerName nickname del giocatore da rimuovere
     */
    public void removePlayer(String playerName) {
        ClientHandler client = players.remove(playerName);

        if (client != null) {
            client.close();
        }
    }

    /**
     * Disconnette un giocatore mantenendo lo stato.
     * Questa operazione:
     * <ul>
     *   <li>Rimuove il giocatore dal registro</li>
     *   <li>Aggiorna lo stato del gioco</li>
     *   <li>Notifica gli altri giocatori</li>
     *   <li>Aggiorna la lista dei personaggi disponibili</li>
     * </ul>
     *
     * @param playerName nickname del giocatore da disconnettere
     */
    public void disconnectPlayer(String playerName) {
        ClientHandler client = players.remove(playerName);

        gameEngine.disconnectPlayer(playerName);

        if (client != null) {
            client.setUpdateHandler(GameServer.getRoomManager());
            client.sendMessage(new RoomDisconnectedPacket(name));
        }
        broadcastPacket(new RoomDetailsPacket(name, getPlayerNames()));
        broadcastPacket(PacketFactory.fromGameAvailableCharacters(gameEngine.getAvailableCharacters()));
    }

    /**
     * Invia un pacchetto a tutti i giocatori connessi.
     *
     * @param packet pacchetto da trasmettere
     */
    public void broadcastPacket(Packet packet) {
        players.values().forEach(c -> c.sendMessage(packet));
    }

    /**
     * Elabora un comando di gioco.
     * Questo metodo:
     * <ul>
     *   <li>Verifica la validità del comando</li>
     *   <li>Esegue il comando tramite il motore di gioco</li>
     *   <li>Distribuisce le risposte ai client interessati</li>
     *   <li>Persiste il nuovo stato di gioco</li>
     * </ul>
     *
     * @param client client che ha inviato il comando
     * @param packet pacchetto contenente il comando
     */
    public synchronized void handleGameCommand(ClientHandler client, GameCommandPacket packet) {
        packet.setPlayerName(findPlayerName(client));

        List<CommandHandler.CommandResponse> commandResponseList = gameEngine.handleCommand(packet.getPlayerName(), packet.getCommand());

        if (commandResponseList == null || commandResponseList.isEmpty()) {
            client.sendMessage(new ErrorPacket(ErrorCode.COMMAND_NOT_AVAILABLE));
            return;
        }

        for (CommandHandler.CommandResponse commandResponse : commandResponseList) {
            if (commandResponse.isBroadcast()) {
                broadcastPacket(commandResponse.getPacket());
            } else {
                client.sendMessage(commandResponse.getPacket());
            }
        }

        GameStateDAO gameStateDAO = DAOFactory.createGameStateDAO(DbManager.getConnection(), gameEngine.getGameDescription());
        try {
            gameStateDAO.save(name, gameEngine.getGameState());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Gestisce i pacchetti di rete ricevuti dai client.
     * Elabora:
     * <ul>
     *   <li>Gestione della stanza (eliminazione, disconnessione)</li>
     *   <li>Richieste di informazioni</li>
     *   <li>Messaggi di chat</li>
     *   <li>Comandi di gioco</li>
     *   <li>Selezione dei personaggi</li>
     * </ul>
     *
     * @param client client mittente
     * @param update pacchetto ricevuto
     */
    @Override
    public void onUpdate(ClientHandler client, Packet update) {
        switch (update.getType()) {
            case DELETE_ROOM -> {
                DeleteRoomPacket deleteRoomPacket = (DeleteRoomPacket) update;

                if (!deleteRoomPacket.getRoomName().equals(name)) {
                    client.sendMessage(new ErrorPacket(ErrorCode.WRONG_ROOM));
                    return;
                }

                if (!GameServer.getRoomManager().removeGameRoom(name)){
                    client.sendMessage(new ErrorPacket(ErrorCode.ROOM_NOT_DELETED));
                    return;
                }

                broadcastPacket(new RoomDeletedPacket(name));
                getPlayerNames().forEach(this::disconnectPlayer);

            }
            case DISCONNECT_ROOM -> disconnectPlayer(findPlayerName(client));
            case GET_ROOM_DETAILS -> {
                GetRoomDetailsPacket getRoomDetailsPacket = (GetRoomDetailsPacket) update;
                String roomName = getRoomDetailsPacket.getRoomName();
                if (!roomName.equalsIgnoreCase(name)) {
                    client.sendMessage(new ErrorPacket(ErrorCode.WRONG_ROOM));
                    return;
                }

                client.sendMessage(new RoomDetailsPacket(roomName, getPlayerNames()));
            }

            case CHAT_MESSAGE -> {
                ChatMessagePacket chatMessagePacket = (ChatMessagePacket) update;
                chatMessagePacket.setPlayerName(findPlayerName(client));
                broadcastPacket(chatMessagePacket);
            }

            case GAME_COMMAND -> handleGameCommand(client, (GameCommandPacket) update);
            case GAME_SELECT_CHARACTER -> {
                GameSelectCharacterPacket gameSelectCharacterPacket = (GameSelectCharacterPacket) update;

                String playerName = findPlayerName(client);
                String character = gameSelectCharacterPacket.getCharacter();

                if (gameEngine.connectPlayer(playerName, character)) {
                    broadcastPacket(PacketFactory.fromGameAvailableCharacters(gameEngine.getAvailableCharacters()));
                } else {
                    client.sendMessage(new ErrorPacket(ErrorCode.CHARACTER_NOT_AVAILABLE));
                }
            }
            case GAME_GET_AVAILABLE_CHARACTERS -> client.sendMessage(PacketFactory.fromGameAvailableCharacters(gameEngine.getAvailableCharacters()));

            case JOIN_ROOM, CREATE_ROOM, GET_ROOM_LIST -> client.sendMessage(new ErrorPacket(ErrorCode.ALREADY_IN_ROOM));
            case null -> client.sendMessage(new ErrorPacket(ErrorCode.INVALID_PACKET));
            default -> client.sendMessage(new ErrorPacket(ErrorCode.UNRECOGNIZED_PACKET));
        }
    }

    /**
     * Gestisce la disconnessione improvvisa di un client.
     * <p>
     * Rimuove il giocatore e notifica gli altri partecipanti.
     * </p>
     *
     * @param client client disconnesso
     */
    @Override
    public void onDisconnect(ClientHandler client) {
        removePlayer(findPlayerName(client));
        broadcastPacket(new RoomDetailsPacket(name, getPlayerNames()));
    }
}