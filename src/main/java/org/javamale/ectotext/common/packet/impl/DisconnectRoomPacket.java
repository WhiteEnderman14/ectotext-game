package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;

/**
 * Pacchetto inviato da un client per <strong>abbandonare</strong> la stanza
 * a cui è già connesso. Il server, ricevuta la richiesta, rimuoverà il
 * giocatore dalla stanza e, se necessario, notificherà gli altri utenti.
 * <p>
 * Campo contenuto:
 * <ul>
 *   <li>{@link #roomName} – nome pubblico della stanza da cui disconnettersi.</li>
 * </ul>
 * Formato JSON risultante:
 * <pre>{
 *   "type": "disconnect_room",
 *   "room_name": "SalaProva"
 * }</pre>
 */
public class DisconnectRoomPacket extends Packet {

    /** Nome della stanza da cui il client desidera disconnettersi. */
    private String roomName;

    /** Costruttore vuoto necessario a Gson/Reflection per la deserializzazione. */
    public DisconnectRoomPacket() {
        super(PacketType.DISCONNECT_ROOM);
    }

    /**
     * Costruisce un pacchetto specificando la stanza.
     *
     * @param roomName nome pubblico della stanza
     */
    public DisconnectRoomPacket(String roomName) {
        this();
        this.roomName = roomName;
    }

    /** @return nome della stanza da cui disconnettersi */
    public String getRoomName() {
        return roomName;
    }

    /** Aggiorna il nome della stanza. */
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
