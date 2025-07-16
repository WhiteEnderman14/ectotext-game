package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;

/**
 * Pacchetto <em>vuoto</em> che il client invia al server per richiedere
 * l’elenco delle stanze (lobby) attualmente disponibili.
 * <p>
 * Il server risponderà con {@code RoomListPacket} che conterrà la lista
 * delle stanze e i loro dati riepilogativi (numero giocatori, password,
 * capienza, ecc.).
 * Non sono necessari campi aggiuntivi oltre al tipo, quindi i metodi
 * {@link #toJson(JsonWriter)} e {@link #fromJson(JsonReader)} non scrivono
 * né leggono attributi ulteriori.
 */
public class GetRoomListPacket extends Packet {

    /** Costruttore vuoto – nessun parametro richiesto. */
    public GetRoomListPacket() {
        super(PacketType.GET_ROOM_LIST);
    }

    /* Nessun campo da serializzare oltre al "type". */
    @Override
    protected void toJson(JsonWriter writer) throws IOException {
        // No-op: pacchetto privo di payload
    }

    /* Nessun campo da deserializzare oltre al "type". */
    @Override
    protected void fromJson(JsonReader reader) throws IOException {
        // No-op: pacchetto privo di payload
    }
}
