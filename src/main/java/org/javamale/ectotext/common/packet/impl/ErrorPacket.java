package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.ErrorCode;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;

/**
 * Pacchetto di errore inviato dal server al client (o vice-versa) per
 * notificare che una richiesta non è andata a buon fine.
 * <p>
 * Contiene:
 * <ul>
 *   <li>{@link #errorCode} – codice numerico/enum che identifica il problema;</li>
 *   <li>{@link #errorMessage} – messaggio descrittivo da mostrare all’utente
 *       (se non fornito, viene usato {@link ErrorCode#getDefaultMessage()}).</li>
 * </ul>
 * Esempio di JSON serializzato:
 * <pre>{
 *   "type": "error",
 *   "error_code": 203,
 *   "error_message": "Wrong password"
 * }</pre>
 */
public class ErrorPacket extends Packet {

    /** Codice di errore secondo l’enum {@link ErrorCode}. */
    private ErrorCode errorCode;

    /** Messaggio da visualizzare al client. */
    private String errorMessage;

    /** Costruttore vuoto necessario per la deserializzazione via reflection. */
    public ErrorPacket() {
        super(PacketType.ERROR);
    }

    /**
     * Costruisce un pacchetto di errore con codice e messaggio personalizzato.
     *
     * @param errorCode    enum che identifica il problema
     * @param errorMessage testo da mostrare all’utente
     */
    public ErrorPacket(ErrorCode errorCode, String errorMessage) {
        this();
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Costruisce un pacchetto di errore usando il messaggio di default
     * associato al codice.
     *
     * @param errorCode enum del problema
     */
    public ErrorPacket(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage());
    }

    /** @return codice di errore */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /** @return messaggio di errore */
    public String getErrorMessage() {
        return errorMessage;
    }

    /** Aggiorna il codice di errore. */
    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    /** Aggiorna il messaggio di errore. */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /* ====================================================================== */
    /*                       SERIALIZZAZIONE / GSON                           */
    /* ====================================================================== */

    /** {@inheritDoc} */
    @Override
    protected void toJson(JsonWriter writer) throws IOException {
        writer.name("error_code").value(errorCode.getCode());
        writer.name("error_message").value(errorMessage);
    }

    /** {@inheritDoc} */
    @Override
    protected void fromJson(JsonReader reader) throws IOException {
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "error_code"    -> errorCode = ErrorCode.fromCode(reader.nextInt());
                case "error_message" -> errorMessage = reader.nextString();
                default              -> throw new IllegalStateException(
                        "Unexpected JSON field: " + name);
            }
        }
    }
}
