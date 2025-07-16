package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;

/**
 * Pacchetto che il client invia al server per richiedere i dettagli di una
 * stanza/lobby specifica, identificata dal suo nome pubblico.
 * <p>
 * Il server – se la stanza esiste – risponderà con {@code RoomDetailsPacket}
 * contenente le informazioni richieste; in caso contrario restituirà un
 * {@code ErrorPacket}.
 * <br>
 * JSON di esempio:
 * <pre>{
 *   "type": "get_room",
 *   "room_name": "SalaProva"
 * }</pre>
 */
public class GetRoomDetailsPacket extends Packet {

    /** Nome della stanza di cui si vogliono ottenere i dettagli. */
    private String roomName;

    /** Costruttore vuoto necessario per deserializzazione via reflection. */
    public GetRoomDetailsPacket() {
        super(PacketType.GET_ROOM_DETAILS);
    }

    /**
     * Costruisce il pacchetto con il nome della stanza desiderata.
     *
     * @param roomName nome pubblico della stanza
     */
    public GetRoomDetailsPacket(String roomName) {
        super(PacketType.GET_ROOM_DETAILS);

        this.roomName = roomName;
    }

    /**
     * Restituisce il nome pubblico della stanza di cui si vogliono ottenere i dettagli.
     *
     * @return il nome della stanza richiesta
     */
    public String getRoomName() {
        return roomName;
    }

    /**
     * Imposta il nome pubblico della stanza di cui si vogliono ottenere i dettagli.
     *
     * @param roomName il nuovo nome della stanza da richiedere
     */
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
