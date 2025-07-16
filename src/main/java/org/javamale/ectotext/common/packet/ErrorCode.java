package org.javamale.ectotext.common.packet;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Enumerazione di tutti i codici di errore gestiti dal protocollo.
 * <p>
 * La suddivisione in fasce è la seguente:
 * <pre>
 *   0       : Nessun errore
 *   1–99    : Errori generali
 *   100–199 : Errori di protocollo
 *   200–299 : Errori relativi alle stanze (Room)
 *   300–399 : Errori di gioco (Game)
 * </pre>
 * Ogni costante enum incapsula:
 * <ul>
 *   <li>un <strong>codice numerico</strong> ({@link #code});</li>
 *   <li>un <strong>messaggio di default</strong> ({@link #defaultMessage})
 *       che può essere visualizzato al client.</li>
 * </ul>
 */
public enum ErrorCode {

    /* ----------------- GENERIC / PROTOCOL ------------------ */
    NO_ERROR(0, "No error"),
    UNRECOGNIZED_PACKET(1, "Packet not recognized"),

    INVALID_PACKET(101, "Packet invalid or malformed"),
    NOT_IN_ROOM(102, "You are not in a room"),
    ALREADY_IN_ROOM(103, "You are already in a room"),

    /* ----------------------- ROOM -------------------------- */
    FULL_ROOM(201, "Room already full"),
    NICKNAME_ALREADY_USED(202, "Nickname already used in this room"),
    WRONG_ROOM_PASSWORD(203, "Wrong password"),
    ROOM_NOT_FOUND(204, "Room not found"),
    ROOM_ALREADY_EXISTS(205, "Room already exists"),
    WRONG_ROOM(206, "Wrong room"),
    ROOM_NOT_CREATED(207, "Room not created"),
    ROOM_NOT_DELETED(208, "Room not deleted"),

    /* ------------------------ GAME ------------------------- */
    // Messaggi in italiano visibili al client
    COMMAND_NOT_AVAILABLE(301, "Comando non disponibile"),
    COMMAND_WITHOUT_ARGS(302, "Parametri mancanti"),
    CHARACTER_NOT_AVAILABLE(303, "Personaggio non disponibile"),
    CHARACTER_NOT_FOUND(304, "Personaggio non trovato"),
    NO_ROOM(310, "Non sei ancora un fantasma, non puoi attraversare i muri"),
    LOCKED_ROOM(311, "La porta è chiusa");

    // ---------------------------------------------------------

    /** Codice numerico univoco dell’errore. */
    private final int code;

    /** Messaggio di default associato al codice. */
    private final String defaultMessage;

    ErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    /** @return codice numerico dell’errore */
    public int getCode() {
        return code;
    }

    /** @return messaggio di default associato al codice */
    public String getDefaultMessage() {
        return defaultMessage;
    }

    /* ---------------------------- LOOKUP ----------------------------- */

    /** Mappa «codice → ErrorCode» per risoluzione rapida. */
    private static final Map<Integer, ErrorCode> CODE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(ErrorCode::getCode, Function.identity()));

    /**
     * Risolve un intero nel corrispondente {@code ErrorCode}.
     *
     * @param code codice numerico
     * @return costante {@link ErrorCode} associata
     * @throws IllegalArgumentException se il codice non è definito
     */
    public static ErrorCode fromCode(int code) {
        ErrorCode errorCode = CODE_MAP.get(code);
        if (errorCode == null) {
            throw new IllegalArgumentException("Unknown ErrorCode: " + code);
        }
        return errorCode;
    }
}
