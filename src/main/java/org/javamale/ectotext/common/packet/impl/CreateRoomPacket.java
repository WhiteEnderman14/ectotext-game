package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;

/**
 * Pacchetto inviato dal client al server per richiedere la creazione di una
 * nuova stanza (lobby) multigiocatore.
 * <p>
 * Campi principali:
 * <ul>
 *   <li>{@link #roomName} – nome pubblico della stanza da creare;</li>
 *   <li>{@link #roomPassword} – eventuale password richiesta per entrare
 *       (può essere {@code null} o stringa vuota se la stanza è aperta).</li>
 * </ul>
 * La serializzazione JSON ha il formato:
 * <pre>{
 *   "type": "create_room",
 *   "room_name": "SalaProva",
 *   "room_password": "1234"
 * }</pre>
 */
public class CreateRoomPacket extends Packet {

    /** Nome della stanza che il client desidera creare. */
    private String roomName;

    /** Password facoltativa per l’accesso alla stanza. */
    private String roomPassword;

    /** Costruttore vuoto richiesto per la deserializzazione via reflection. */
    public CreateRoomPacket() {
        super(PacketType.CREATE_ROOM);
    }

    /**
     * Costruisce un pacchetto con nome stanza e password opzionale.
     *
     * @param roomName     nome pubblico della stanza
     * @param roomPassword password di accesso (può essere {@code null})
     */
    public CreateRoomPacket(String roomName, String roomPassword) {
        this();
        this.roomName = roomName;
        this.roomPassword = roomPassword;
    }

    /** @return nome della stanza da creare */
    public String getRoomName() {
        return roomName;
    }

    /** @return password di accesso (può essere {@code null} o vuota) */
    public String getRoomPassword() {
        return roomPassword;
    }

    /** Aggiorna il nome della stanza. */
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    /** Aggiorna la password della stanza. */
    public void setRoomPassword(String roomPassword) {
        this.roomPassword = roomPassword;
    }

    /* ====================================================================== */
    /*                       SERIALIZZAZIONE / GSON                           */
    /* ====================================================================== */

    /** {@inheritDoc} */
    @Override
    protected void toJson(JsonWriter writer) throws IOException {
        writer.name("room_name").value(roomName);
        writer.name("room_password").value(roomPassword);
    }

    /** {@inheritDoc} */
    @Override
    protected void fromJson(JsonReader reader) throws IOException {
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "room_name"     -> roomName     = reader.nextString();
                case "room_password" -> roomPassword = reader.nextString();
                default              -> throw new IllegalStateException(
                        "Unexpected JSON field: " + name);
            }
        }
    }
}
