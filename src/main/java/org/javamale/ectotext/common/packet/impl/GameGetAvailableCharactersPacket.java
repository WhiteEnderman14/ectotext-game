package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;

/**
 * Pacchetto <em>vuoto</em> che il client invia al server per richiedere
 * l’elenco dei personaggi ancora selezionabili.
 * <p>
 * Il server risponde con {@link GameAvailableCharactersPacket} contenente
 * i nomi disponibili. Poiché non occorrono campi aggiuntivi oltre al
 * {@code type}, i metodi {@link #toJson(JsonWriter)} e
 * {@link #fromJson(JsonReader)} non scrivono/leggono ulteriori attributi.
 */
public class GameGetAvailableCharactersPacket extends Packet {

    /** Costruttore vuoto – non sono necessari parametri. */
    public GameGetAvailableCharactersPacket() {
        super(PacketType.GAME_GET_AVAILABLE_CHARACTERS);
    }

    /* Nessun campo da serializzare oltre al "type". */
    @Override
    protected void toJson(JsonWriter writer) throws IOException {
        // No-op: nessun dato specifico da scrivere
    }

    /* Nessun campo da leggere oltre al "type". */
    @Override
    protected void fromJson(JsonReader reader) throws IOException {
        // No-op: nessun dato specifico da leggere
    }
}
