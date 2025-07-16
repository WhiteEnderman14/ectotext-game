package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;

/**
 * Pacchetto che il client invia al server per <strong>entrare</strong> in una
 * stanza (lobby) esistente.
 * Se la stanza richiede una password, quest’ultima è inclusa; in caso di
 * successo il server risponderà con {@code RoomJoinedPacket}, mentre in caso
 * di errore verrà inviato un {@code ErrorPacket}.
 * <p>
 * Campi:
 * <ul>
 *   <li>{@link #playerName} – nome (nickname) del giocatore che entra;</li>
 *   <li>{@link #roomName} – nome pubblico della stanza;</li>
 *   <li>{@link #roomPassword} – password (può essere {@code null} o vuota).</li>
 * </ul>
 * Formato JSON esemplificativo:
 * <pre>{
 *   "type": "join_room",
 *   "player_name": "Alice",
 *   "room_name": "SalaProva",
 *   "room_password": "1234"
 * }</pre>
 */
public class JoinRoomPacket extends Packet {

    /** Nome del giocatore che desidera entrare nella stanza. */
    private String playerName;

    /** Nome della stanza a cui connettersi. */
    private String roomName;

    /** Password della stanza (se richiesta). */
    private String roomPassword;

    /** Costruttore vuoto necessario a Gson/Reflection per deserializzazione. */
    public JoinRoomPacket() {
        super(PacketType.JOIN_ROOM);
    }

    /**
     * Costruisce un pacchetto con tutti i campi necessari.
     *
     * @param playerName  nickname del giocatore
     * @param roomName    nome della stanza
     * @param roomPassword password della stanza (può essere {@code null})
     */
    public JoinRoomPacket(String playerName, String roomName, String roomPassword) {
        this();
        this.roomName = roomName;
        this.playerName = playerName;
        this.roomPassword = roomPassword;
    }

    /* ------------------------------------------------------------------ */
    /*                               GETTER                               */
    /* ------------------------------------------------------------------ */

    /** @return nome del giocatore */
    public String getPlayerName() {
        return playerName;
    }

    /** @return nome della stanza */
    public String getRoomName() {
        return roomName;
    }

    /** @return password della stanza (può essere {@code null}) */
    public String getRoomPassword() {
        return roomPassword;
    }

    /* ------------------------------------------------------------------ */
    /*                               SETTER                               */
    /* ------------------------------------------------------------------ */

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setRoomPassword(String roomPassword) {
        this.roomPassword = roomPassword;
    }

    /* ====================================================================== */
    /*                       SERIALIZZAZIONE / GSON                           */
    /* ====================================================================== */

    /** {@inheritDoc} */
    @Override
    protected void toJson(JsonWriter writer) throws IOException {
        writer.name("player_name").value(playerName);
        writer.name("room_name").value(roomName);
        writer.name("room_password").value(roomPassword);
    }

    /** {@inheritDoc} */
    @Override
    protected void fromJson(JsonReader reader) throws IOException {
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "player_name"   -> playerName   = reader.nextString();
                case "room_name"     -> roomName     = reader.nextString();
                case "room_password" -> roomPassword = reader.nextString();
                default              -> throw new IllegalStateException(
                        "Unexpected JSON field: " + name);
            }
        }
    }
}
