package org.javamale.ectotext.common.model;

import java.util.*;

/**
 * Rappresenta un personaggio nel mondo di gioco.
 * <p>
 * Ogni {@code Character} possiede:
 * <ul>
 *   <li>un <strong>nome interno</strong> (ereditato da {@link Entity}) usato come identificatore univoco;</li>
 *   <li>un <strong>display name</strong> leggibile dall’utente;</li>
 *   <li>una <strong>stanza di partenza</strong> (anch’essa ereditata) che indica dove viene creato;</li>
 *   <li>un <strong>inventario</strong> di oggetti ({@link Item}).</li>
 * </ul>
 * Il personaggio può aggiungere/rimuovere oggetti, verificarne la presenza
 * e trasferirli ad altri {@code Character}.
 */
public class Character extends Entity {

    /** Nome visualizzato nei messaggi di gioco e nelle interfacce. */
    protected String displayName;

    /** Insieme degli oggetti posseduti dal personaggio. */
    protected final Set<Item> inventory;

    /**
     * Costruisce un nuovo {@code Character}.
     *
     * @param characterName nome interno (univoco) del personaggio
     * @param displayName   nome visualizzato all’utente
     * @param startingRoom  stanza iniziale in cui compare il personaggio
     */
    public Character(String characterName, String displayName, Room startingRoom) {
        super(characterName, startingRoom);

        this.displayName = displayName;
        this.inventory = new HashSet<>();
    }

    /**
     * Restituisce il nome visualizzato del personaggio.
     *
     * @return display name attuale
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Aggiorna il nome visualizzato del personaggio.
     *
     * @param displayName nuovo display name da impostare
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Restituisce una vista <em>sola lettura</em> dell’inventario.
     *
     * @return insieme non modificabile degli oggetti posseduti
     */
    public Set<Item> getInventory() {
        return Collections.unmodifiableSet(inventory);
    }

    /**
     * Aggiunge un oggetto all’inventario.
     *
     * @param item oggetto da aggiungere
     */
    public void addItem(Item item) {
        inventory.add(item);
    }

    /**
     * Rimuove dall’inventario l’oggetto con il nome indicato (case-insensitive).
     *
     * @param itemName nome dell’oggetto da rimuovere
     * @return {@code true} se l’oggetto è stato trovato e rimosso, {@code false} altrimenti
     */
    public boolean removeItem(String itemName) {
        return inventory.removeIf(i -> i.getName().equalsIgnoreCase(itemName));
    }

    /**
     * Verifica se nell’inventario è presente un oggetto con il nome indicato (case-insensitive).
     *
     * @param itemName nome dell’oggetto da cercare
     * @return {@code true} se l’oggetto è presente, {@code false} altrimenti
     */
    public boolean hasItem(String itemName) {
        return inventory.stream().anyMatch(i -> i.getName().equalsIgnoreCase(itemName));
    }

    /**
     * Trasferisce un oggetto dall’inventario di questo personaggio a quello del {@code target}.
     *
     * @param itemName nome dell’oggetto da trasferire
     * @param target   personaggio che riceverà l’oggetto
     * @return {@code true} se il trasferimento è andato a buon fine, {@code false} se l’oggetto non è stato trovato
     */
    public boolean transferItemTo(String itemName, Character target) {
        Optional<Item> foundItem = inventory.stream()
                .filter(i -> i.getName().equalsIgnoreCase(itemName))
                .findFirst();

        if (foundItem.isEmpty()) {
            return false;
        }

        Item item = foundItem.get();
        inventory.remove(item);
        target.addItem(item);

        // Eventuale notifica/log aggiuntiva può essere implementata qui.
        return true;
    }

    /**
     * Ritorna il {@code displayName}; utile per debug e log.
     */
    @Override
    public String toString() {
        return displayName;
    }

    // ---------------------------------------------------------------------
    //                          BUILDER PATTERN
    // ---------------------------------------------------------------------

    /**
     * Builder per costruire in modo fluente un {@code Character}.
     * <p>
     * Esempio d’uso:
     * <pre>{@code
     * Character hero = new Character.CharacterBuilder("hero")
     *         .setDisplayName("Eroe")
     *         .setStartingRoom(startRoom)
     *         .addItems(sword, shield)
     *         .build();
     * }</pre>
     */
    public static class CharacterBuilder {
        /** Nome interno univoco (obbligatorio). */
        private final String name;

        /** Nome visualizzato (obbligatorio). */
        private String displayName;

        /** Stanza iniziale (obbligatoria). */
        private Room startingRoom;

        /** Inventario iniziale (opzionale, vuoto di default). */
        private final Set<Item> inventory;

        /**
         * Crea un builder specificando il nome interno del personaggio.
         *
         * @param name nome interno univoco
         */
        public CharacterBuilder(String name) {
            this.name = name;
            this.inventory = new HashSet<>();
        }

        /**
         * Costruisce il {@code Character} verificando che i campi obbligatori
         * {@link #displayName} e {@link #startingRoom} siano stati impostati.
         *
         * @return nuova istanza di {@code Character}
         * @throws NullPointerException se display name o starting room sono {@code null}
         */
        public Character build() {
            Objects.requireNonNull(displayName, "Il display name non può essere null");
            Objects.requireNonNull(startingRoom, "La starting room non può essere null");

            Character character = new Character(name, displayName, startingRoom);
            inventory.forEach(character::addItem);
            return character;
        }

        /**
         * Imposta il nome visualizzato del personaggio.
         *
         * @param displayName nome leggibile dall’utente
         * @return il builder stesso per chiamate fluide
         */
        public CharacterBuilder setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * Imposta la stanza di partenza del personaggio.
         *
         * @param startingRoom stanza iniziale
         * @return il builder stesso per chiamate fluide
         */
        public CharacterBuilder setStartingRoom(Room startingRoom) {
            this.startingRoom = startingRoom;
            return this;
        }

        /**
         * Aggiunge un oggetto all’inventario iniziale.
         *
         * @param item oggetto da aggiungere
         * @return il builder stesso per chiamate fluide
         */
        public CharacterBuilder addItem(Item item) {
            inventory.add(item);
            return this;
        }

        /**
         * Aggiunge una collezione di oggetti all’inventario iniziale.
         *
         * @param items collezione di oggetti
         * @return il builder stesso per chiamate fluide
         */
        public CharacterBuilder addItems(Collection<Item> items) {
            inventory.addAll(items);
            return this;
        }

        /**
         * Aggiunge più oggetti all’inventario iniziale usando var-args.
         *
         * @param items uno o più oggetti
         * @return il builder stesso per chiamate fluide
         */
        public CharacterBuilder addItems(Item... items) {
            return addItems(Arrays.asList(items));
        }
    }
}
