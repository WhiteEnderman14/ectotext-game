package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;

/**
 * Pacchetto inviato da un client al server per richiedere la
 * <strong>cancellazione di una stanza</strong> già esistente.
 * <p>
 * Contiene un unico campo:
 * <ul>
 *   <li>{@link #roomName} – nome pubblico della stanza da eliminare.</li>
 * </ul>
 * Esempio di JSON serializzato:
 * <pre>{
 *   "type": "delete_room",
 *   "room_name": "SalaProva"
 * }</pre>
 */
public class DeleteRoomPacket extends Packet {

    /** Nome della stanza che si desidera eliminare. */
    private String roomName;

    /** Costruttore vuoto richiesto per deserializzazione via reflection. */
    public DeleteRoomPacket() {
        super(PacketType.DELETE_ROOM);
    }

    /**
     * Costruisce un pacchetto con il nome stanza specificato.
     *
     * @param roomName nome pubblico della stanza da cancellare
     */
    public DeleteRoomPacket(String roomName) {
        this();
        this.roomName = roomName;
    }

    /** @return nome della stanza da cancellare */
    public String getRoomName() {
        return roomName;
    }

    /** Aggiorna il nome della stanza da cancellare. */
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
                throw new IllegalStateException("Unexpected JSON field: " + name);
            }
        }
    }
}
