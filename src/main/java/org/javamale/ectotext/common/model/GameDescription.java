package org.javamale.ectotext.common.model;

import java.util.Map;

/**
 * Descrive in modo <em>statico</em> le caratteristiche di un gioco.
 * <p>
 * Contiene tre componenti fondamentali:
 * <ul>
 *   <li><strong>Nome del gioco</strong> ({@link #gameName}) – identificativo
 *       leggibile che appare nei menu o nei log;</li>
 *   <li><strong>Mappa del gioco</strong> ({@link #gameMap}) – insieme delle
 *       stanze, collegamenti e descrizioni ambientali;</li>
 *   <li><strong>Comandi disponibili</strong> ({@link #gameCommands}) – mappa
 *       che associa la stringa digitata dal giocatore al relativo
 *       {@link CommandHandler} che la gestisce.</li>
 * </ul>
 * Questa classe è immutabile: tutti i campi sono definitivi e impostati
 * tramite costruttore.
 */
public class GameDescription {

    /** Nome leggibile del gioco (es. “Ecto Text: La Cripta”). */
    private final String gameName;

    /** Mappa completa delle stanze e dei collegamenti direzionali. */
    private final GameMap gameMap;

    /** Dizionario «comando → handler» per la fase di parsing dell’input. */
    private final Map<String, CommandHandler> gameCommands;

    /**
     * Costruisce una descrizione di gioco.
     *
     * @param gameName      nome del gioco
     * @param gameMap       mappa (stanze, direzioni, descrizioni)
     * @param gameCommands  mappa di handler per i comandi supportati
     */
    public GameDescription(String gameName,
                           GameMap gameMap,
                           Map<String, CommandHandler> gameCommands) {
        this.gameName = gameName;
        this.gameMap = gameMap;
        this.gameCommands = gameCommands;
    }

    /**
     * Restituisce il nome leggibile del gioco.
     *
     * @return nome del gioco
     */
    public String getGameName() {
        return gameName;
    }

    /**
     * Restituisce la mappa statica del gioco.
     *
     * @return istanza di {@link GameMap}
     */
    public GameMap getGameMap() {
        return gameMap;
    }

    /**
     * Restituisce la mappa «comando → handler».
     *
     * @return {@code Map} di handler registrati
     */
    public Map<String, CommandHandler> getGameCommands() {
        return gameCommands;
    }
}
