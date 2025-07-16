package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;

/**
 * Pacchetto che trasporta un <em>comando di gioco</em> digitato da un
 * giocatore e destinato al motore del server (parser).
 * <p>
 * Contiene due campi essenziali:
 * <ul>
 *   <li>{@link #playerName} – nome del giocatore che invia il comando;</li>
 *   <li>{@link #command} – stringa del comando completo (ad esempio
 *       {@code "usa chiave porta_nord"}).</li>
 * </ul>
 * Esempio JSON:
 * <pre>{
 *   "type": "game_command",
 *   "player_name": "Alice",
 *   "command": "look north"
 * }</pre>
 */
public class GameCommandPacket extends Packet {

    /** Nome del giocatore mittente. */
    private String playerName;

    /** Comando testuale da eseguire. */
    private String command;

    /** Costruttore vuoto richiesto da reflection/Gson. */
    public GameCommandPacket() {
        super(PacketType.GAME_COMMAND);
    }

    /**
     * Costruisce un pacchetto comando con mittente e comando.
     *
     * @param playerName nome del giocatore
     * @param command    stringa comando
     */
    public GameCommandPacket(String playerName, String command) {
        this();
        this.playerName = playerName;
        this.command = command;
    }

    /** @return nome del giocatore mittente */
    public String getPlayerName() {
        return playerName;
    }

    /** Imposta/aggiorna il nome del mittente. */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    /** @return stringa comando da eseguire */
    public String getCommand() {
        return command;
    }

    /** Aggiorna il comando. */
    public void setCommand(String command) {
        this.command = command;
    }

    /* ====================================================================== */
    /*                       SERIALIZZAZIONE / GSON                           */
    /* ====================================================================== */

    /** {@inheritDoc} */
    @Override
    protected void toJson(JsonWriter writer) throws IOException {
        writer.name("player_name").value(playerName);
        writer.name("command").value(command);
    }

    /** {@inheritDoc} */
    @Override
    protected void fromJson(JsonReader reader) throws IOException {
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "player_name" -> playerName = reader.nextString();
                case "command"     -> command    = reader.nextString();
                default            -> throw new IllegalStateException(
                        "Unexpected JSON field: " + name);
            }
        }
    }
}
