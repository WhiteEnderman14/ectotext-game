package org.javamale.ectotext.server.core;

import org.javamale.ectotext.common.model.*;
import org.javamale.ectotext.common.model.Character;
import org.javamale.ectotext.common.packet.ErrorCode;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.impl.ErrorPacket;
import org.javamale.ectotext.common.packet.impl.GameNarratorPacket;

import java.util.*;

/**
 * Motore di gioco che gestisce la logica e lo stato della partita multiplayer.
 * Questo componente è responsabile di:
 * <ul>
 *   <li>Gestire lo stato del gioco e la sua evoluzione</li>
 *   <li>Coordinare l'esecuzione dei comandi dei giocatori</li>
 *   <li>Gestire l'associazione giocatori-personaggi</li>
 *   <li>Controllare le fasi di gioco (introduzione, partita)</li>
 * </ul>
 * 
 * <p>
 * La classe è thread-safe: tutti i metodi pubblici che modificano lo stato
 * sono sincronizzati per garantire la coerenza in ambiente multiplayer.
 * </p>
 *
 * @see GameCreator
 * @see GameState
 * @see GameDescription
 * @see Character
 */
public class GameEngine {
    
    /**
     * Factory per la creazione degli elementi di gioco.
     * Utilizzato per inizializzare e reimpostare lo stato del gioco.
     */
    private final GameCreator gameCreator;

    /**
     * Configurazione statica del gioco.
     * Contiene definizioni di mappe, comandi disponibili e regole.
     */
    private final GameDescription gameDescription;

    /**
     * Stato dinamico della partita.
     * Include posizioni dei personaggi, inventari e variabili di gioco.
     */
    private GameState gameState;

    /**
     * Registro delle associazioni giocatore-personaggio.
     * Thread-safe mediante sincronizzazione esterna.
     */
    private final Map<String, Character> playerCharacters;

    /**
     * Flag che indica se il gioco è nella fase introduttiva.
     * Durante questa fase vengono mostrati i messaggi di benvenuto.
     */
    private boolean intro;

    /**
     * Inizializza un nuovo motore di gioco.
     * Questo costruttore:
     * <ul>
     *   <li>Crea la descrizione del gioco tramite il creator</li>
     *   <li>Inizializza lo stato di default</li>
     *   <li>Prepara il registro dei giocatori</li>
     *   <li>Attiva la fase introduttiva</li>
     * </ul>
     *
     * @param gameCreator factory per la creazione degli elementi di gioco
     * @throws IllegalArgumentException se gameCreator è null
     */
    public GameEngine(GameCreator gameCreator) {
        this.gameCreator = gameCreator;
        this.gameDescription = gameCreator.createGameDescription();
        this.gameState = gameCreator.createDefaultGameState(gameDescription);

        this.playerCharacters = new HashMap<>();
        this.intro = true;
    }

    /**
     * Ottiene la configurazione statica del gioco.
     *
     * @return descrizione immutabile del gioco
     */
    public GameDescription getGameDescription() {
        return gameDescription;
    }

    /**
     * Ottiene lo stato corrente della partita.
     *
     * @return stato attuale del gioco
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Aggiorna lo stato della partita.
     * <p>
     * Utilizzato principalmente per il ripristino da persistenza.
     * </p>
     *
     * @param gameState nuovo stato da applicare
     * @throws IllegalArgumentException se gameState è null
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Verifica se il gioco è in fase introduttiva.
     *
     * @return {@code true} se è attiva l'introduzione
     */
    public boolean isIntro() {
        return intro;
    }

    /**
     * Imposta la fase di gioco.
     *
     * @param intro {@code true} per attivare l'introduzione
     */
    public void setIntro(boolean intro) {
        this.intro = intro;
    }

    /**
     * Ottiene i messaggi introduttivi del gioco.
     * <p>
     * Se l'introduzione è stata completata, restituisce solo
     * il messaggio di selezione personaggio.
     * </p>
     *
     * @return lista di pacchetti da mostrare al giocatore
     * @see Packet
     */
    public synchronized List<Packet> getIntroPackets() {
        if (this.intro) {
            return gameCreator.gameIntro();
        } else {
            return List.of(
                    new GameNarratorPacket("Seleziona un personaggio...")
            );
        }
    }

    /**
     * Associa un giocatore a un personaggio.
     * L'operazione fallisce se:
     * <ul>
     *   <li>Il personaggio non esiste</li>
     *   <li>Il personaggio è già stato scelto</li>
     * </ul>
     *
     * @param playerName identificatore del giocatore
     * @param characterName nome del personaggio scelto
     * @return {@code true} se l'associazione ha successo
     */
    public synchronized boolean connectPlayer(String playerName, String characterName) {
        Character character = gameState.getCharacter(characterName);
        if (character == null) {
            return false;
        }
        playerCharacters.put(playerName, character);
        return true;
    }

    /**
     * Rimuove l'associazione di un giocatore al suo personaggio.
     *
     * @param playerName identificatore del giocatore
     */
    public synchronized void disconnectPlayer(String playerName) {
        playerCharacters.remove(playerName);
    }

    /**
     * Rimuove tutte le associazioni giocatore-personaggio.
     * Utilizzato durante lo shutdown del gioco.
     */
    public synchronized void disconnectAllPlayers() {
        playerCharacters.clear();
    }

    /**
     * Elenca i personaggi non ancora scelti dai giocatori.
     *
     * @return collezione immutabile dei personaggi disponibili
     */
    public synchronized Collection<Character> getAvailableCharacters() {
        return gameState.getCharacters().stream().filter(c -> !playerCharacters.containsValue(c)).toList();
    }

    /**
     * Elabora ed esegue un comando di gioco.
     * Questo metodo:
     * <ul>
     *   <li>Analizza il comando testuale</li>
     *   <li>Verifica la disponibilità del comando</li>
     *   <li>Controlla l'associazione al personaggio</li>
     *   <li>Esegue il comando se valido</li>
     *   <li>Termina la fase introduttiva</li>
     * </ul>
     *
     * @param playerName giocatore che esegue il comando
     * @param commandString comando in formato testuale
     * @return lista delle risposte generate dal comando
     * @see CommandHandler.CommandResponse
     */
    public synchronized List<CommandHandler.CommandResponse> handleCommand(String playerName, String commandString) {
        String[] tokens = commandString.split("\\s+");
        String command = Arrays.stream(tokens)
                .filter(gameDescription.getGameCommands().keySet()::contains)
                .findFirst().orElse(null);

        if (command == null) {
            return List.of(new CommandHandler.CommandResponse(
                    new ErrorPacket(ErrorCode.COMMAND_NOT_AVAILABLE),
                    false
            ));
        }

        Character character = playerCharacters.get(playerName);

        if (character == null) {
            return List.of(new CommandHandler.CommandResponse(
                    new ErrorPacket(ErrorCode.CHARACTER_NOT_FOUND, "Seleziona un personaggio per giocare"),
                    false
            ));
        }

        intro = false;

        return gameDescription.getGameCommands().get(command).execute(character, gameDescription, gameState, tokens);
    }
}