package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;

/**
 * Pacchetto di messaggistica testuale inviato da un client a tutti gli
 * utenti nella stessa stanza (lobby o partita) e vice-versa.
 * <p>
 * Campi principali:
 * <ul>
 *   <li>{@link #playerName} – nome del mittente mostrato nella chat;</li>
 *   <li>{@link #message} – contenuto testuale del messaggio.</li>
 * </ul>
 * La serializzazione JSON produce una struttura del tipo:
 * <pre>{
 *   "type": "chat_message",
 *   "player_name": "Alice",
 *   "message": "Ciao a tutti!"
 * }</pre>
 */
public class ChatMessagePacket extends Packet {

    /** Nome del giocatore che invia il messaggio. */
    private String playerName;

    /** Testo del messaggio inviato. */
    private String message;

    /** Costruttore senza argomenti richiesto da reflection/deserializzazione. */
    public ChatMessagePacket() {
        super(PacketType.CHAT_MESSAGE);
    }

    /**
     * Costruisce un pacchetto di chat con mittente e testo.
     *
     * @param playerName nome del giocatore
     * @param message    contenuto del messaggio
     */
    public ChatMessagePacket(String playerName, String message) {
        this();
        this.playerName = playerName;   // <— (campo inizializzato)
        this.message = message;
    }

    /** @return nome del mittente */
    public String getPlayerName() {
        return playerName;
    }

    /** @return contenuto testuale del messaggio */
    public String getMessage() {
        return message;
    }

    /**
     * Aggiorna il messaggio (utile per pooling/riutilizzo oggetti).
     *
     * @param message nuovo testo
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Aggiorna il nome del mittente.
     *
     * @param playerName nome giocatore
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    /* ====================================================================== */
    /*                       SERIALIZZAZIONE / GSON                           */
    /* ====================================================================== */

    /** {@inheritDoc} */
    @Override
    protected void toJson(JsonWriter writer) throws IOException {
        writer.name("player_name").value(playerName);
        writer.name("message").value(message);
    }

    /** {@inheritDoc} */
    @Override
    protected void fromJson(JsonReader reader) throws IOException {
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "player_name" -> playerName = reader.nextString();
                case "message"     -> message    = reader.nextString();
                default            -> throw new IllegalStateException(
                        "Unexpected JSON field: " + name);
            }
        }
    }
}
