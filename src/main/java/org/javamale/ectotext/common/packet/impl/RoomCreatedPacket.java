package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;

/**
 * Pacchetto di conferma inviato dal server quando una nuova stanza
 * è stata creata con successo.
 * <p>
 * Contiene un unico campo:
 * <ul>
 *   <li>{@link #roomName} – nome pubblico della stanza appena creata.</li>
 * </ul>
 * JSON di esempio:
 * <pre>{
 *   "type": "room_created",
 *   "room_name": "SalaProva"
 * }</pre>
 */
public class RoomCreatedPacket extends Packet {

    /** Nome della stanza appena creata. */
    private String roomName;

    /** Costruttore vuoto necessario per deserializzazione via reflection. */
    public RoomCreatedPacket() {
        super(PacketType.ROOM_CREATED);
    }

    /**
     * Costruisce il pacchetto con il nome della stanza.
     *
     * @param roomName nome pubblico della stanza
     */
    public RoomCreatedPacket(String roomName) {
        this();
        this.roomName = roomName;
    }

    /** @return nome della stanza creata */
    public String getRoomName() {
        return roomName;
    }

    /** Aggiorna il nome della stanza (in casi di riutilizzo oggetto). */
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
