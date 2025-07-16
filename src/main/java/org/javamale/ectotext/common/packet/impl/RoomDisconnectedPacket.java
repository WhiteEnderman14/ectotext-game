package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;

/**
 * Pacchetto di notifica inviato dal server quando un giocatore
 * si è disconnesso (lasciato) dalla stanza specificata.
 * <p>
 * Contiene un unico campo:
 * <ul>
 *   <li>{@link #roomName} – nome pubblico della stanza da cui l’utente
 *       si è disconnesso.</li>
 * </ul>
 * Formato JSON di esempio:
 * <pre>{
 *   "type": "room_disconnected",
 *   "room_name": "SalaProva"
 * }</pre>
 */
public class RoomDisconnectedPacket extends Packet {

    /** Nome della stanza da cui il client è stato disconnesso. */
    private String roomName;

    /** Costruttore vuoto necessario per deserializzazione via reflection. */
    public RoomDisconnectedPacket() {
        super(PacketType.ROOM_DISCONNECTED);
    }

    /**
     * Costruisce il pacchetto con il nome della stanza.
     *
     * @param roomName nome pubblico della stanza
     */
    public RoomDisconnectedPacket(String roomName) {
        this();
        this.roomName = roomName;
    }

    /** @return nome della stanza da cui si è usciti */
    public String getRoomName() {
        return roomName;
    }

    /** Imposta/aggiorna il nome della stanza. */
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    /* ====================================================================== */
    /*                       SERIALIZZAZIONE / GSON                           */
    /* ====================================================================== */

    /** {@inheritDoc} */
    @Override
    protected void toJson(JsonWriter writer) throws IOException {
        writer.name("room_name").value(roomName);
    }

    /** {@inheritDoc} */
    @Override
    protected void fromJson(JsonReader reader) throws IOException {
        while (reader.hasNext()) {
            String name = reader.nextName();
            if ("room_name".equals(name)) {
                roomName = reader.nextString();
            } else {
                throw new IllegalStateException(
                        "Unexpected JSON field: " + name);
            }
        }
    }
}
