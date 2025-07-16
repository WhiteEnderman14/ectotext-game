package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;

/**
 * Pacchetto che veicola un messaggio del <strong>Narratore</strong>
 * (testo descrittivo, ambientale, eventi di gioco) verso i client.
 * <p>
 * Contiene un solo campo:
 * <ul>
 *   <li>{@link #message} – testo narrativo da mostrare sul client.</li>
 * </ul>
 * Esempio JSON:
 * <pre>{
 *   "type": "game_narrator",
 *   "message": "Una folata di vento gelido attraversa il corridoio..."
 * }</pre>
 */
public class GameNarratorPacket extends Packet {

    /** Testo narrativo da visualizzare ai giocatori. */
    private String message;

    /** Costruttore vuoto richiesto per deserializzazione via reflection. */
    public GameNarratorPacket() {
        super(PacketType.GAME_NARRATOR);
    }

    /**
     * Costruisce un pacchetto con il messaggio narrativo indicato.
     *
     * @param message testo narratore
     */
    public GameNarratorPacket(String message) {
        this();
        this.message = message;
    }

    /** @return messaggio del narratore */
    public String getMessage() {
        return message;
    }

    /** Aggiorna il messaggio narrativo. */
    public void setMessage(String message) {
        this.message = message;
    }

    /* ====================================================================== */
    /*                       SERIALIZZAZIONE / GSON                           */
    /* ====================================================================== */

    /** {@inheritDoc} */
    @Override
    protected void toJson(JsonWriter writer) throws IOException {
        writer.name("message").value(message);
    }

    /** {@inheritDoc} */
    @Override
    protected void fromJson(JsonReader reader) throws IOException {
        while (reader.hasNext()) {
            String name = reader.nextName();
            if ("message".equals(name)) {
                message = reader.nextString();
            } else {
                throw new IllegalStateException(
                        "Unexpected JSON field: " + name);
            }
        }
    }
}
