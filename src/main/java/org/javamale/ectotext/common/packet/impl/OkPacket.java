package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;

/**
 * Pacchetto di conferma (“OK”) inviato dal server al client (o vice-versa)
 * quando un’operazione si è conclusa con successo senza necessità di dati
 * aggiuntivi.
 * Facoltativamente può contenere un breve messaggio informativo
 * ({@link #okMessage}).
 *
 * <p>Esempio JSON senza messaggio:</p>
 * <pre>{
 *   "type": "ok"
 * }</pre>
 *
 * <p>Esempio con messaggio:</p>
 * <pre>{
 *   "type": "ok",
 *   "ok_message": "Stanza creata con successo"
 * }</pre>
 */
public class OkPacket extends Packet {

    /** Messaggio opzionale da mostrare al client. */
    private String okMessage;

    /** Costruttore vuoto richiesto per deserializzazione via reflection. */
    public OkPacket() {
        super(PacketType.OK);
    }

    /**
     * Costruisce un pacchetto OK con messaggio personalizzato.
     *
     * @param okMessage testo informativo opzionale
     */
    public OkPacket(String okMessage) {
        this();
        this.okMessage = okMessage;
    }

    /** @return messaggio di conferma, o {@code null} se assente */
    public String getOkMessage() {
        return okMessage;
    }

    /** Aggiorna il messaggio di conferma. */
    public void setOkMessage(String okMessage) {
        this.okMessage = okMessage;
    }

    /* ====================================================================== */
    /*                       SERIALIZZAZIONE / GSON                           */
    /* ====================================================================== */

    /** {@inheritDoc} */
    @Override
    protected void toJson(JsonWriter writer) throws IOException {
        writer.name("ok_message").value(okMessage);
    }

    /** {@inheritDoc} */
    @Override
    protected void fromJson(JsonReader reader) throws IOException {
        while (reader.hasNext()) {
            String name = reader.nextName();
            if ("ok_message".equals(name)) {
                okMessage = reader.nextString();
            } else {
                throw new IllegalStateException(
                        "Unexpected JSON field: " + name);
            }
        }
    }
}
