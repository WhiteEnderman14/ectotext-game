package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;
import java.util.*;

/**
 * Pacchetto inviato dal server in risposta a
 * {@link GetRoomDetailsPacket}: contiene i dettagli di una stanza/lobby
 * specifica.
 * <p>
 * Campi trasportati:
 * <ul>
 *   <li>{@link #roomName} – nome pubblico della stanza;</li>
 *   <li>{@link #userCount} – numero di giocatori attualmente presenti;</li>
 *   <li>{@link #users} – elenco dei nomi dei giocatori presenti.</li>
 * </ul>
 * Esempio JSON:
 * <pre>{
 *   "type": "room_details",
 *   "room_name": "SalaProva",
 *   "user_count": 2,
 *   "users": ["Alice", "Bob"]
 * }</pre>
 */
public class RoomDetailsPacket extends Packet {

    /** Nome della stanza. */
    private String roomName;

    /** Numero corrente di utenti nella stanza. */
    private int userCount;

    /** Lista (ordine preservato) dei nomi degli utenti. */
    private final List<String> users;

    /* ------------------------------------------------------------------ */
    /*                             COSTRUTTORI                            */
    /* ------------------------------------------------------------------ */

    /** Costruttore vuoto necessario a Gson/Reflection. */
    public RoomDetailsPacket() {
        super(PacketType.ROOM_DETAILS);
        users = new ArrayList<>();
    }

    /**
     * Costruisce il pacchetto con nome stanza e collezione utenti.
     *
     * @param roomName nome pubblico della stanza
     * @param users    collezione di nomi utente
     */
    public RoomDetailsPacket(String roomName, Collection<String> users) {
        this();
        this.roomName = roomName;
        this.userCount = users.size();
        this.users.addAll(users);
    }

    /** Variante var-args di {@link #RoomDetailsPacket(String, Collection)}. */
    public RoomDetailsPacket(String roomName, String... users) {
        this(roomName, Arrays.stream(users).toList());
    }

    /* ------------------------------------------------------------------ */
    /*                               GETTER                               */
    /* ------------------------------------------------------------------ */

    /** @return nome della stanza */
    public String getRoomName() {
        return roomName;
    }

    /** @return numero di utenti presenti */
    public int getUserCount() {
        return userCount;
    }

    /** @return lista live dei nomi utente */
    public List<String> getUsers() {
        return users;
    }

    /* ------------------------------------------------------------------ */
    /*                    METODI DI MUTAZIONE LISTA                       */
    /* ------------------------------------------------------------------ */

    /** Aggiunge un singolo utente. */
    public void addUser(String entry) {
        this.users.add(entry);
    }

    /** Aggiunge una collezione di utenti. */
    public void addAllRooms(Collection<String> entries) {
        this.users.addAll(entries);
    }

    /** Variante var-args di {@link #addAllRooms(Collection)}. */
    public void addAllRooms(String... entries) {
        Collections.addAll(this.users, entries);
    }

    /* ====================================================================== */
    /*                       SERIALIZZAZIONE / GSON                           */
    /* ====================================================================== */

    /** {@inheritDoc} */
    @Override
    protected void toJson(JsonWriter writer) throws IOException {
        writer.name("room_name").value(roomName);
        writer.name("user_count").value(userCount);
        writer.name("users");
        writer.beginArray();
        users.forEach(user -> {
            try {
                writer.value(user);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });
        writer.endArray();
    }

    /** {@inheritDoc} */
    @Override
    protected void fromJson(JsonReader reader) throws IOException {
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "room_name"  -> roomName  = reader.nextString();
                case "user_count" -> userCount = reader.nextInt();
                case "users" -> {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        addUser(reader.nextString());
                    }
                    reader.endArray();
                }
                default -> throw new IllegalStateException(
                        "Unexpected JSON field: " + name);
            }
        }
    }
}
