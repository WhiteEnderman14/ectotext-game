package org.javamale.ectotext.server.util;

import org.javamale.ectotext.common.model.Character;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;
import org.javamale.ectotext.common.packet.impl.GameAvailableCharactersPacket;
import org.javamale.ectotext.common.packet.impl.RoomDetailsPacket;
import org.javamale.ectotext.common.packet.impl.RoomListPacket;
import org.javamale.ectotext.server.core.GameRoom;

import java.util.Collection;

/**
 * Factory per la creazione di pacchetti di comunicazione.
 * Questa classe implementa il pattern Factory e fornisce metodi statici per convertire
 * gli oggetti del dominio in pacchetti di rete. Si occupa di:
 * <ul>
 *   <li>Conversione da oggetti di dominio a pacchetti di rete</li>
 *   <li>Creazione di pacchetti informativi sullo stato del gioco</li>
 *   <li>Standardizzazione del formato dei pacchetti</li>
 * </ul>
 * 
 * @see Packet
 * @see PacketType
 */
public class PacketFactory {

    /**
     * Crea un pacchetto contenente la lista delle stanze disponibili.
     * Converte una collezione di {@link GameRoom} in un {@link RoomListPacket},
     * estraendo per ogni stanza:
     * <ul>
     *   <li>Nome della stanza</li>
     *   <li>Numero di giocatori presenti</li>
     * </ul>
     *
     * @param gameRoomList collezione di stanze di gioco attive
     * @return pacchetto contenente le informazioni essenziali di ogni stanza
     * @throws NullPointerException se gameRoomList è null
     * @see RoomListPacket
     * @see GameRoom
     */
    public static RoomListPacket fromGameRoomList(Collection<GameRoom> gameRoomList) {
        Collection<RoomListPacket.RoomListEntry> entries = gameRoomList.stream()
                .map(g -> new RoomListPacket.RoomListEntry(g.getName(), g.getPlayerCount()))
                .toList();

        return new RoomListPacket(entries);
    }

    /**
     * Crea un pacchetto contenente i dettagli di una specifica stanza.
     * Converte un {@link GameRoom} in un {@link RoomDetailsPacket},
     * includendo:
     * <ul>
     *   <li>Nome della stanza</li>
     *   <li>Lista completa dei nomi dei giocatori presenti</li>
     * </ul>
     *
     * @param gameRoom stanza di gioco di cui creare il pacchetto informativo
     * @return pacchetto contenente i dettagli della stanza
     * @throws NullPointerException se gameRoom è null
     * @see RoomDetailsPacket
     * @see GameRoom#getPlayerNames()
     */
    public static RoomDetailsPacket fromGameRoom(GameRoom gameRoom) {
        return new RoomDetailsPacket(gameRoom.getName(), gameRoom.getPlayerNames());
    }

    /**
     * Crea un pacchetto contenente la lista dei personaggi disponibili.
     * Converte una collezione di {@link Character} in un {@link GameAvailableCharactersPacket},
     * estraendo solo i nomi dei personaggi. Questo pacchetto viene utilizzato per:
     * <ul>
     *   <li>Informare i client sui personaggi selezionabili</li>
     *   <li>Aggiornare l'interfaccia di selezione personaggio</li>
     * </ul>
     *
     * @param characters collezione di personaggi disponibili nel gioco
     * @return pacchetto contenente i nomi dei personaggi disponibili
     * @throws NullPointerException se characters è null
     * @see GameAvailableCharactersPacket
     * @see Character
     */
    public static GameAvailableCharactersPacket fromGameAvailableCharacters(Collection<org.javamale.ectotext.common.model.Character> characters) {
        Collection<String> entries = characters.stream()
                .map(Character::getName)
                .toList();

        return new GameAvailableCharactersPacket(entries);
    }
}