package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;

/**
 * Pacchetto di notifica inviato dal server ai client presenti nella stanza
 * quando un nuovo giocatore vi si unisce con successo.
 * <p>
 * Campi:
 * <ul>
 *   <li>{@link #playerName} – nickname del giocatore appena entrato;</li>
 *   <li>{@link #roomName} – nome pubblico della stanza.</li>
 * </ul>
 * Esempio JSON:
 * <pre>{
 *   "type": "room_joined",
 *   "player_name": "Alice",
 *   "room_name": "SalaProva"
 * }</pre>
 */
public class RoomJoinedPacket extends Packet {

    /** Nome del giocatore che è entrato nella stanza. */
    private String playerName;

    /** Nome della stanza in cui il giocatore è entrato. */
    private String roomName;

    /** Costruttore vuoto richiesto per deserializzazione via reflection. */
    public RoomJoinedPacket() {
        super(PacketType.ROOM_JOINED);
    }

    /**
     * Costruisce il pacchetto con nome giocatore e stanza.
     *
     * @param playerName nickname del giocatore
     * @param roomName   nome pubblico della stanza
     */
    public RoomJoinedPacket(String playerName, String roomName) {
        this();
        this.roomName = roomName;
        this.playerName = playerName;
    }

    /** @return nome del giocatore entrato */
    public String getPlayerName() {
        return playerName;
    }

    /** @return nome della stanza di destinazione */
    public String getRoomName() {
        return roomName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    /* ====================================================================== */
    /*                       SERIALIZZAZIONE / GSON                           */
    /* ====================================================================== */

    /** {@inheritDoc} */
    @Override
    protected void toJson(JsonWriter writer) throws IOException {
        writer.name("player_name").value(playerName);
        writer.name("room_name").value(roomName);
    }

    /** {@inheritDoc} */
    @Override
    protected void fromJson(JsonReader reader) throws IOException {
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "player_name" -> playerName = reader.nextString();
                case "room_name"   -> roomName   = reader.nextString();
                default            -> throw new IllegalStateException(
                        "Unexpected JSON field: " + name);
            }
        }
    }
}
