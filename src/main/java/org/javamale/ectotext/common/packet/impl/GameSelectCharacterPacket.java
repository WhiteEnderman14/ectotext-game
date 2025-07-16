package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;

/**
 * Pacchetto inviato da un client per <strong>prenotare</strong> uno dei
 * personaggi disponibili prima dell’avvio della partita.
 * <p>
 * Contiene:
 * <ul>
 *   <li>{@link #playerName} – nome del giocatore che effettua la scelta;</li>
 *   <li>{@link #character} – nome del personaggio selezionato.</li>
 * </ul>
 * Il server risponderà con esito positivo o con un
 * {@link org.javamale.ectotext.common.packet.impl.ErrorPacket} se il
 * personaggio non è più disponibile.
 * <br>
 * Esempio JSON:
 * <pre>{
 *   "type": "game_select_character",
 *   "player_name": "Alice",
 *   "character": "Egon"
 * }</pre>
 */
public class GameSelectCharacterPacket extends Packet {

    /** Nome del giocatore che sta scegliendo il personaggio. */
    private String playerName;

    /** Personaggio scelto dal giocatore. */
    private String character;

    /** Costruttore vuoto necessario per deserializzazione via reflection. */
    public GameSelectCharacterPacket() {
        super(PacketType.GAME_SELECT_CHARACTER);
    }

    /**
     * Costruisce il pacchetto con mittente e personaggio scelto.
     *
     * @param playerName nome del giocatore
     * @param character  nome del personaggio selezionato
     */
    public GameSelectCharacterPacket(String playerName, String character) {
        this();
        this.playerName = playerName;
        this.character = character;
    }

    /** @return nome del giocatore mittente */
    public String getPlayerName() {
        return playerName;
    }

    /** Imposta/aggiorna il nome del giocatore. */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    /** @return nome del personaggio scelto */
    public String getCharacter() {
        return character;
    }

    /** Aggiorna il personaggio scelto. */
    public void setCharacter(String character) {
        this.character = character;
    }

    /* ====================================================================== */
    /*                       SERIALIZZAZIONE / GSON                           */
    /* ====================================================================== */

    /** {@inheritDoc} */
    @Override
    protected void toJson(JsonWriter writer) throws IOException {
        writer.name("player_name").value(playerName);
        writer.name("character").value(character);
    }

    /** {@inheritDoc} */
    @Override
    protected void fromJson(JsonReader reader) throws IOException {
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "player_name" -> playerName = reader.nextString();
                case "character"   -> character  = reader.nextString();
                default            -> throw new IllegalStateException(
                        "Unexpected JSON field: " + name);
            }
        }
    }
}
