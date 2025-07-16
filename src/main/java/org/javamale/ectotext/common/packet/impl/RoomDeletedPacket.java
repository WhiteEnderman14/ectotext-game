package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;

/**
 * Pacchetto di notifica inviato dal server quando una stanza esistente
 * è stata eliminata (ad esempio dall’host o perché vuota).
 * <p>
 * Contiene un solo campo:
 * <ul>
 *   <li>{@link #roomName} – nome pubblico della stanza eliminata.</li>
 * </ul>
 * Formato JSON:
 * <pre>{
 *   "type": "room_deleted",
 *   "room_name": "SalaProva"
 * }</pre>
 */
public class RoomDeletedPacket extends Packet {

    /** Nome della stanza eliminata. */
    private String roomName;

    /** Costruttore vuoto necessario a Gson/Reflection. */
    public RoomDeletedPacket() {
        super(PacketType.ROOM_DELETED);
    }

    /**
     * Costruisce il pacchetto con il nome stanza da notificare.
     *
     * @param roomName nome pubblico della stanza eliminata
     */
    public RoomDeletedPacket(String roomName) {
        this();
        this.roomName = roomName;
    }

    /** @return nome della stanza eliminata */
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
