package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;

/**
 * Pacchetto che trasporta una <em>battuta di dialogo</em> generata dal server
 * (tipicamente da un NPC o dal narratore) e destinata ai client.
 * <p>
 * Campi:
 * <ul>
 *   <li>{@link #speaker} – nome del personaggio/NPC che parla
 *       (può essere “Narratore” o simile);</li>
 *   <li>{@link #message} – testo della battuta.</li>
 * </ul>
 * Formato JSON risultante:
 * <pre>{
 *   "type": "game_dialogue",
 *   "speaker": "Egon",
 *   "message": "Attento! C'è ectoplasma ovunque."
 * }</pre>
 */
public class GameDialoguePacket extends Packet {

    /** Nome del personaggio o entità che pronuncia la battuta. */
    private String speaker;

    /** Contenuto testuale del dialogo. */
    private String message;

    /** Costruttore vuoto necessario per deserializzazione via reflection. */
    public GameDialoguePacket() {
        super(PacketType.GAME_DIALOGUE);
    }

    /**
     * Costruisce un pacchetto di dialogo con speaker e messaggio.
     *
     * @param speaker nome del parlante (NPC, narratore, ecc.)
     * @param message testo del dialogo
     */
    public GameDialoguePacket(String speaker, String message) {
        this();
        this.speaker = speaker;
        this.message = message;
    }

    /** @return nome del parlante */
    public String getSpeaker() {
        return speaker;
    }

    /** Imposta/aggiorna il parlante. */
    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    /** @return testo del messaggio di dialogo */
    public String getMessage() {
        return message;
    }

    /** Aggiorna il messaggio. */
    public void setMessage(String message) {
        this.message = message;
    }

    /* ====================================================================== */
    /*                       SERIALIZZAZIONE / GSON                           */
    /* ====================================================================== */

    /** {@inheritDoc} */
    @Override
    protected void toJson(JsonWriter writer) throws IOException {
        writer.name("speaker").value(speaker);
        writer.name("message").value(message);
    }

    /** {@inheritDoc} */
    @Override
    protected void fromJson(JsonReader reader) throws IOException {
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "speaker" -> speaker = reader.nextString();
                case "message" -> message  = reader.nextString();
                default        -> throw new IllegalStateException(
                        "Unexpected JSON field: " + name);
            }
        }
    }
}
