package org.javamale.ectotext.common.packet;

import org.javamale.ectotext.common.packet.impl.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumerazione di tutti i tipi di pacchetto scambiati tra client e server.
 * <p>
 * Ogni costante associa:
 * <ul>
 *   <li>una <strong>chiave testuale</strong> (campo {@link #key}) che viaggia
 *       effettivamente sul filo;</li>
 *   <li>la <strong>classe concreta</strong> ({@link #clazz}) che implementa
 *       {@link Packet} e incapsula i dati del messaggio.</li>
 * </ul>
 * È inoltre disponibile una mappa di lookup «chiave → {@code PacketType}»
 * per risolvere in modo efficiente un pacchetto in arrivo.
 */
public enum PacketType {
    /* ------------------------------------------------------------- */
    /*                    RISPOSTE GENERICHE                         */
    /* ------------------------------------------------------------- */
    OK("ok", OkPacket.class),
    ERROR("error", ErrorPacket.class),

    /* ------------------------ LOBBY / ROOM ----------------------- */
    GET_ROOM_LIST("get_rooms", GetRoomListPacket.class),
    GET_ROOM_DETAILS("get_room", GetRoomDetailsPacket.class),
    CREATE_ROOM("create_room", CreateRoomPacket.class),
    JOIN_ROOM("join_room", JoinRoomPacket.class),
    DISCONNECT_ROOM("disconnect_room", DisconnectRoomPacket.class),
    DELETE_ROOM("delete_room", DeleteRoomPacket.class),

    ROOM_LIST("room_list", RoomListPacket.class),
    ROOM_DETAILS("room_details", RoomDetailsPacket.class),
    ROOM_CREATED("room_created", RoomCreatedPacket.class),
    ROOM_JOINED("room_joined", RoomJoinedPacket.class),
    ROOM_DISCONNECTED("room_disconnected", RoomDisconnectedPacket.class),
    ROOM_DELETED("room_deleted", RoomDeletedPacket.class),

    CHAT_MESSAGE("chat_message", ChatMessagePacket.class),

    /* ---------------------------- GAME --------------------------- */
    GAME_GET_AVAILABLE_CHARACTERS("game_get_available_characters", GameGetAvailableCharactersPacket.class),
    GAME_SELECT_CHARACTER("game_select_character", GameSelectCharacterPacket.class),
    GAME_COMMAND("game_command", GameCommandPacket.class),

    GAME_AVAILABLE_CHARACTERS("game_available_characters", GameAvailableCharactersPacket.class),
    GAME_NARRATOR("game_narrator", GameNarratorPacket.class),
    GAME_DIALOGUE("game_dialogue", GameDialoguePacket.class);

    /* ------------------------------------------------------------- */

    /** Chiave serializzabile del pacchetto (unica). */
    private final String key;

    /** Classe concreta che implementa il pacchetto. */
    private final Class<? extends Packet> clazz;

    /** Mappa di lookup «key → PacketType», popolata staticamente. */
    private static final Map<String, PacketType> LOOKUP;

    /* Blocco statico per costruire la mappa di lookup e rilevare duplicati. */
    static {
        Map<String, PacketType> map = new HashMap<>();
        for (PacketType type : values()) {
            if (map.containsKey(type.getKey())) {
                throw new IllegalStateException("Duplicate key: " + type.getKey());
            }
            map.put(type.getKey(), type);
        }
        LOOKUP = Collections.unmodifiableMap(map);
    }

    /**
     * Associa la chiave testuale alla classe del pacchetto.
     *
     * @param key   chiave univoca
     * @param clazz classe concreta che estende {@link Packet}
     */
    PacketType(final String key, final Class<? extends Packet> clazz) {
        this.key = key;
        this.clazz = clazz;
    }

    /** @return chiave testuale del pacchetto (usata in serializzazione) */
    public String getKey() {
        return key;
    }

    /**
     * Crea un’istanza <strong>vuota</strong> del pacchetto associato,
     * usando reflection sul costruttore di default.
     *
     * @return nuova istanza di {@link Packet}
     * @throws NoSuchMethodException    se manca il costruttore di default
     * @throws InvocationTargetException propagazione di eccezioni del costruttore
     * @throws InstantiationException   se la classe è astratta
     * @throws IllegalAccessException   se il costruttore non è accessibile
     */
    public Packet getPacket() throws NoSuchMethodException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException {
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * Risolve la chiave testuale nel corrispondente {@code PacketType}.
     *
     * @param key chiave da ricercare
     * @return tipo di pacchetto, o {@code null} se non esiste
     */
    public static PacketType fromKey(String key) {
        return LOOKUP.get(key);
    }
}
