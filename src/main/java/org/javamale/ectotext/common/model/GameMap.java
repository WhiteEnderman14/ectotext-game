package org.javamale.ectotext.common.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Modello della mappa statica del gioco.
 * <p>
 * La mappa tiene traccia di tutte le {@link Room} registrate
 * e fornisce metodi per:
 * <ul>
 *   <li>aggiungere stanze tramite {@link #addRoom(Room)};</li>
 *   <li>recuperarle per nome in modo case-insensitive con {@link #getRoom(String)};</li>
 *   <li>ottenere una vista immutabile di tutte le stanze tramite {@link #getAllRooms()}.</li>
 * </ul>
 * <b>Nota:</b> questa classe non verifica l’unicità dei nomi né gestisce
 * la logica interna del collegamento (che deve essere implementata nelle
 * rispettive {@code Room}) — qui si occupa solo di mantenerne la collezione.
 */
public class GameMap {

    /** Dizionario «nome stanza → stanza» in cui le chiavi sono normalizzate in lowercase. */
    private final Map<String, Room> rooms;

    /** Costruisce una mappa vuota, pronta per accogliere stanze. */
    public GameMap() {
        this.rooms = new HashMap<>();
    }

    /**
     * Aggiunge una stanza alla mappa.
     *
     * @param room stanza da registrare
     */
    public void addRoom(Room room) {
        rooms.put(room.getName(), room);
    }

    /**
     * Restituisce la stanza con il nome indicato (case-insensitive).
     *
     * @param name nome della stanza da cercare
     * @return la stanza se presente, altrimenti {@code null}
     */
    public Room getRoom(String name) {
        return rooms.get(name.toLowerCase());
    }

    /**
     * Restituisce una vista <em>sola lettura</em> di tutte le stanze registrate.
     *
     * @return mappa immutabile «nome → stanza»
     */
    public Map<String, Room> getAllRooms() {
        return Map.copyOf(rooms);
    }
}
