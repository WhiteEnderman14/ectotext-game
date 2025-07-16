package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;
import java.util.*;

/**
 * Pacchetto inviato dal server in risposta a {@code GetRoomListPacket} e
 * contenente l’elenco delle stanze attualmente disponibili.
 * <p>
 * Ogni stanza è rappresentata da un {@link RoomListEntry} che riporta
 * il nome pubblico della lobby e il numero di giocatori connessi.
 * <p>
 * Struttura JSON di esempio:
 * <pre>{
 *   "type": "room_list",
 *   "rooms": [
 *     { "room_name": "SalaProva", "user_count": 2 },
 *     { "room_name": "Lobby2",    "user_count": 0 }
 *   ]
 * }</pre>
 */
public class RoomListPacket extends Packet {

    /** Lista (ordine preservato) dei dettagli delle stanze. */
    private final List<RoomListEntry> roomList;

    /* ------------------------------------------------------------------ */
    /*                             COSTRUTTORI                            */
    /* ------------------------------------------------------------------ */

    /** Costruttore vuoto richiesto per deserializzazione via reflection. */
    public RoomListPacket() {
        super(PacketType.ROOM_LIST);
        roomList = new ArrayList<>();
    }

    /**
     * Costruisce il pacchetto partendo da una collezione di entry.
     *
     * @param roomList collezione di {@link RoomListEntry}
     */
    public RoomListPacket(Collection<RoomListEntry> roomList) {
        this();
        addAllRooms(roomList);
    }

    /** Variante var-args di {@link #RoomListPacket(Collection)}. */
    public RoomListPacket(RoomListEntry... roomList) {
        this();
        addAllRooms(roomList);
    }

    /* ------------------------------------------------------------------ */
    /*                               GETTER                               */
    /* ------------------------------------------------------------------ */

    /** @return lista live delle stanze disponibili */
    public List<RoomListEntry> getRoomList() {
        return roomList;
    }

    /* ------------------------------------------------------------------ */
    /*                    METODI DI MUTAZIONE LISTA                       */
    /* ------------------------------------------------------------------ */

    /** Aggiunge una singola entry alla lista. */
    public void addRoom(RoomListEntry entry) {
        this.roomList.add(entry);
    }

    /** Aggiunge una collezione di entry alla lista. */
    public void addAllRooms(Collection<RoomListEntry> entries) {
        this.roomList.addAll(entries);
    }

    /** Variante var-args di {@link #addAllRooms(Collection)}. */
    public void addAllRooms(RoomListEntry... entries) {
        Collections.addAll(this.roomList, entries);
    }

    /* ====================================================================== */
    /*                       SERIALIZZAZIONE / GSON                           */
    /* ====================================================================== */

    /** {@inheritDoc} */
    @Override
    protected void toJson(JsonWriter writer) throws IOException {
        writer.name("rooms");
        writer.beginArray();
        roomList.forEach(entry -> {
            try {
                writer.beginObject();
                writer.name("room_name").value(entry.name);
                writer.name("user_count").value(entry.userCount);
                writer.endObject();
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
            if ("rooms".equals(name)) {
                reader.beginArray();
                while (reader.hasNext()) {
                    RoomListEntry roomListEntry = readRoomEntry(reader);
                    if (roomListEntry != null) {
                        addRoom(roomListEntry);
                    }
                }
                reader.endArray();
            } else {
                throw new IllegalStateException(
                        "Unexpected JSON field: " + name);
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /*                    METODI DI SUPPORTO PRIVATI                       */
    /* ------------------------------------------------------------------ */

    /**
     * Legge un singolo oggetto stanza dal reader JSON.
     *
     * @param reader JSON reader posizionato all'inizio dell'oggetto
     * @return {@link RoomListEntry} costruito, o {@code null} se dati incompleti
     */
    private RoomListEntry readRoomEntry(JsonReader reader) throws IOException {
        String name = null;
        int userCount = -1;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "room_name"  -> name      = reader.nextString();
                case "user_count" -> userCount = reader.nextInt();
                default           -> reader.skipValue();
            }
        }
        reader.endObject();

        if (name == null || userCount < 0) {
            return null;
        }
        return new RoomListEntry(name, userCount);
    }

    /* ------------------------------------------------------------------ */
    /*                               RECORD                                */
    /* ------------------------------------------------------------------ */

    /**
     * Entry immutabile che rappresenta una stanza nella lista.
     *
     * @param name      nome pubblico della stanza
     * @param userCount numero di giocatori presenti
     */
    public record RoomListEntry(String name, int userCount) {}
}
