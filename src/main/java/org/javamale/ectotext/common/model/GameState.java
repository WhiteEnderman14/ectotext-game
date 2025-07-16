package org.javamale.ectotext.common.model;

import java.util.*;

/**
 * Rappresenta lo <em>stato dinamico</em> di una partita.
 * <p>
 * Contiene e gestisce:
 * <ul>
 *   <li>l’<strong>UUID</strong> univoco della sessione ({@link #uuid});</li>
 *   <li>tutti i <strong>personaggi dei giocatori</strong> presenti ({@link #characters});</li>
 *   <li>gli <strong>NPC</strong> attivi nella partita ({@link #npcs});</li>
 *   <li>la collezione di <strong>flag di gioco</strong> impostati ({@link #flags}).</li>
 * </ul>
 * Fornisce metodi di query (hasX) e di mutazione (add/remove) per ciascuna
 * categoria, oltre a helper sulle collezioni var-args.
 */
public class GameState {

    /** Identificatore univoco della partita. */
    private final UUID uuid;

    /** Mappa «nome personaggio → {@link Character}». */
    private final Map<String, Character> characters;

    /** Mappa «nome NPC → {@link NPC}». */
    private final Map<String, NPC> npcs;

    /** Insieme dei flag di gioco attualmente attivi. */
    private final EnumSet<GameFlag> flags;

    /* ------------------------------------------------------------------ */
    /*                              COSTRUTTORI                           */
    /* ------------------------------------------------------------------ */

    /**
     * Costruisce un nuovo stato di gioco con l’UUID fornito.
     *
     * @param uuid identificatore univoco della sessione
     */
    public GameState(UUID uuid) {
        this.uuid = uuid;

        this.characters = new HashMap<>();
        this.npcs = new HashMap<>();
        this.flags = EnumSet.noneOf(GameFlag.class);
    }

    /** Costruisce un nuovo stato di gioco generando un UUID casuale. */
    public GameState() {
        this(UUID.randomUUID());
    }

    /* ------------------------------------------------------------------ */
    /*                          GETTER DI BASE                            */
    /* ------------------------------------------------------------------ */

    /** @return l’UUID della partita */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Restituisce un personaggio per nome.
     *
     * @param name nome del personaggio
     * @return {@link Character} o {@code null} se non presente
     */
    public Character getCharacter(String name) {
        return characters.get(name);
    }

    /** @return collezione di tutti i personaggi registrati */
    public Collection<Character> getCharacters() {
        return characters.values();
    }

    /**
     * Restituisce un NPC per nome.
     *
     * @param name nome dell’NPC
     * @return {@link NPC} o {@code null} se non presente
     */
    public NPC getNPC(String name) {
        return npcs.get(name);
    }

    /** @return collezione di tutti gli NPC registrati */
    public Collection<NPC> getNPCs() {
        return npcs.values();
    }

    /** @return collezione (live) dei flag di gioco attivi */
    public Collection<GameFlag> getFlags() {
        return flags;
    }

    /* ------------------------------------------------------------------ */
    /*                      METODI DI QUERY (“has…”)                       */
    /* ------------------------------------------------------------------ */

    /**
     * Verifica la presenza del personaggio indicato.
     *
     * @param character personaggio da cercare
     * @return {@code true} se presente
     */
    public boolean hasCharacter(Character character) {
        return characters.containsKey(character.getName());
    }

    /**
     * Verifica che tutti i personaggi passati siano presenti.
     *
     * @param characters collezione di personaggi da controllare
     * @return {@code true} se tutti presenti
     */
    public boolean hasCharacters(Collection<Character> characters) {
        return this.characters.keySet()
                .containsAll(characters.stream()
                        .map(Character::getName)
                        .toList());
    }

    /** Variante var-args di {@link #hasCharacters(Collection)}. */
    public boolean hasCharacters(Character... characters) {
        return hasCharacters(Arrays.asList(characters));
    }

    /**
     * Verifica la presenza dell’NPC indicato.
     *
     * @param npc NPC da cercare
     * @return {@code true} se presente
     */
    public boolean hasNPC(NPC npc) {
        return npcs.containsKey(npc.getName());
    }

    /**
     * Verifica che tutti gli NPC passati siano presenti.
     *
     * @param npcs collezione di NPC da controllare
     * @return {@code true} se tutti presenti
     */
    public boolean hasNPCs(Collection<NPC> npcs) {
        return this.npcs.keySet()
                .containsAll(npcs.stream()
                        .map(NPC::getName)
                        .toList());
    }

    /** Variante var-args di {@link #hasNPCs(Collection)}. */
    public boolean hasNPCs(NPC... npcs) {
        return hasNPCs(Arrays.asList(npcs));
    }

    /**
     * Verifica se un flag di gioco è attivo.
     *
     * @param flag flag da controllare
     * @return {@code true} se presente
     */
    public boolean hasFlag(GameFlag flag) {
        return flags.contains(flag);
    }

    /**
     * Verifica che tutti i flag passati siano attivi.
     *
     * @param flags collezione di flag
     * @return {@code true} se tutti presenti
     */
    public boolean hasFlags(Collection<GameFlag> flags) {
        return this.flags.containsAll(flags);
    }

    /** Variante var-args di {@link #hasFlags(Collection)}. */
    public boolean hasFlags(GameFlag... flags) {
        return hasFlags(Arrays.asList(flags));
    }

    /* ------------------------------------------------------------------ */
    /*                    METODI DI MUTAZIONE (“add…” / remove)           */
    /* ------------------------------------------------------------------ */

    /**
     * Aggiunge un personaggio allo stato di gioco.
     *
     * @param p personaggio da aggiungere
     */
    public void addCharacter(Character p) {
        characters.put(p.getName(), p);
    }

    /**
     * Aggiunge un NPC allo stato di gioco.
     *
     * @param p NPC da aggiungere
     */
    public void addNPC(NPC p) {
        npcs.put(p.getName(), p);
    }

    /**
     * Aggiunge un flag di gioco.
     *
     * @param flag flag da attivare
     */
    public void addFlag(GameFlag flag) {
        flags.add(flag);
    }

    /**
     * Aggiunge più flag di gioco.
     *
     * @param flags collezione di flag
     */
    public void addFlags(Collection<GameFlag> flags) {
        this.flags.addAll(flags);
    }

    /** Variante var-args di {@link #addFlags(Collection)}. */
    public void addFlags(GameFlag... flags) {
        addFlags(Arrays.asList(flags));
    }

    /**
     * Rimuove (disattiva) un flag di gioco.
     *
     * @param flag flag da rimuovere
     * @return {@code true} se il flag era presente e viene rimosso
     */
    public boolean removeFlag(GameFlag flag) {
        return flags.remove(flag);
    }
}
