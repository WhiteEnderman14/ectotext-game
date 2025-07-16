package org.javamale.ectotext.common.model;

import java.util.*;

/**
 * Rappresenta una stanza all’interno della mappa di gioco.
 * <p>
 * Ogni {@code Room} contiene:
 * <ul>
 *   <li>nomi e descrizioni (breve e lunga) mostrati al giocatore;</li>
 *   <li>una lista di oggetti visibili o nascosti presenti nella stanza;</li>
 *   <li>collegamenti direzionali verso altre stanze, opzionalmente
 *       bloccati da uno o più {@link GameFlag};</li>
 *   <li>flag associati a oggetti che ne determinano la visibilità o
 *       l’interazione.</li>
 * </ul>
 * La creazione e configurazione avviene tipicamente tramite il
 * {@link RoomBuilder} per mantenere immutabilità dei campi obbligatori
 * e leggibilità del codice di setup.
 */
public class Room {

    /** Nome interno univoco della stanza (usato come chiave nella mappa). */
    private final String name;

    /** Nome visualizzato al giocatore. */
    private String displayName;
    /** Descrizione breve mostrata nei comandi di “look”. */
    private String description;
    /** Descrizione estesa mostrata al primo ingresso o su comando dedicato. */
    private String longDescription;

    /** Collegamenti «direzione normalizzata → stanza». */
    private Map<String, Room> connections;
    /** Eventuali flag che devono essere attivi per attraversare il collegamento. */
    private Map<String, Collection<GameFlag>> connectionFlags;

    /** Oggetti presenti e visibili nella stanza. */
    private Set<Item> items;
    /** Flag che regolano la visibilità/interazione degli oggetti. */
    private Map<Item, Collection<GameFlag>> itemFlags;

    /* ------------------------------------------------------------------ */
    /*                             COSTRUTTORI                            */
    /* ------------------------------------------------------------------ */

    /**
     * Costruttore privato usato esclusivamente dal {@link RoomBuilder}.
     *
     * @param name nome interno della stanza
     */
    private Room(String name) {
        this.name = name;
    }

    /**
     * Costruttore completo (in genere usato solo nei test).
     *
     * @param name            nome interno
     * @param displayName     nome visualizzato
     * @param description     descrizione breve
     * @param longDescription descrizione estesa
     */
    public Room(String name,
                String displayName,
                String description,
                String longDescription) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.longDescription = longDescription;
        this.connections = new HashMap<>();
        this.connectionFlags = new HashMap<>();
        this.items = new HashSet<>();
        this.itemFlags = new HashMap<>();
    }

    /* ------------------------------------------------------------------ */
    /*                              GETTER                                */
    /* ------------------------------------------------------------------ */

    /** @return nome interno della stanza */
    public String getName() {
        return name;
    }

    /** @return nome visualizzato al giocatore */
    public String getDisplayName() {
        return displayName;
    }

    /** @return descrizione breve */
    public String getDescription() {
        return description;
    }

    /** @return descrizione estesa */
    public String getLongDescription() {
        return longDescription;
    }

    /* ------------------------------------------------------------------ */
    /*                              SETTER                                */
    /* ------------------------------------------------------------------ */

    /** Imposta il nome visualizzato. */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /** Imposta la descrizione breve. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Imposta la descrizione estesa. */
    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    /* ------  Setter privati usati dal builder per iniettare collezioni ----- */

    private void setConnections(Map<String, Room> connections) {
        this.connections = connections;
    }

    private void setConnectionFlags(Map<String, Collection<GameFlag>> connectionFlags) {
        this.connectionFlags = connectionFlags;
    }

    private void setItems(Set<Item> items) {
        this.items = items;
    }

    private void setItemFlags(Map<Item, Collection<GameFlag>> itemFlags) {
        this.itemFlags = itemFlags;
    }

    /* ------------------------------------------------------------------ */
    /*                         METODI DI QUERY                             */
    /* ------------------------------------------------------------------ */

    /**
     * Restituisce la stanza collegata nella direzione specificata.
     * La direzione deve essere già normalizzata (es. “north”, “south”…).
     *
     * @param dir direzione normalizzata
     * @return stanza collegata o {@code null} se inesistente
     */
    public Room getConnectedRoom(String dir) {
        // dir = Direction.normalizeDirection(dir);
        return connections.get(dir);
    }

    /** @return set <em>non modificabile</em> degli oggetti visibili nella stanza */
    public Collection<Item> getItems() {
        return Collections.unmodifiableSet(items);
    }

    /**
     * Controlla se tutti i flag richiesti per attraversare il collegamento
     * nella direzione indicata sono presenti.
     *
     * @param dir   direzione normalizzata
     * @param flags flag di gioco correnti del giocatore
     * @return {@code true} se il passaggio è libero, {@code false} se bloccato
     */
    public boolean checkConnectionFlags(String dir, Collection<GameFlag> flags) {
        if (!connectionFlags.containsKey(dir)) {
            return true;
        }
        return flags.containsAll(connectionFlags.get(dir));
    }

    /**
     * Verifica se l’oggetto è visibile/interagibile in base ai flag correnti.
     *
     * @param item  oggetto da controllare
     * @param flags flag di gioco correnti del giocatore
     * @return {@code true} se l’oggetto può essere visto/usato
     */
    public boolean checkItemFlags(Item item, Collection<GameFlag> flags) {
        if (!itemFlags.containsKey(item)) {
            return true;
        }
        return flags.containsAll(itemFlags.get(item));
    }

    /* ------------------------------------------------------------------ */
    /*                       METODI DI MODIFICA                             */
    /* ------------------------------------------------------------------ */

    /**
     * Collega un’altra stanza in una direzione (non gestisce l’opposto).
     *
     * @param dir  direzione normalizzata
     * @param room stanza di destinazione
     */
    public void connectRoom(String dir, Room room) {
        // dir = Direction.normalizeDirection(dir);
        connections.put(dir, room);
    }

    /**
     * Aggiunge flag di blocco/permesso a un collegamento.
     *
     * @param dir   direzione del collegamento
     * @param flags flag necessari per attraversarlo
     */
    public void addConnectionFlags(String dir, Collection<GameFlag> flags) {
        if (!connectionFlags.containsKey(dir)) {
            connectionFlags.put(dir, new HashSet<>());
        }
        connectionFlags.get(dir).addAll(flags);
    }

    /** Variante var-args di {@link #addConnectionFlags(String, Collection)}. */
    public void addConnectionFlags(String dir, GameFlag... flag) {
        addConnectionFlags(dir, Arrays.asList(flag));
    }

    /** Aggiunge un oggetto visibile nella stanza. */
    public void addItem(Item item) {
        items.add(item);
    }

    /**
     * Associa flag all’oggetto specificato (es. per renderlo visibile solo
     * dopo un certo evento).
     *
     * @param item  oggetto
     * @param flags flag necessari
     */
    public void addItemFlags(Item item, Collection<GameFlag> flags) {
        if (!itemFlags.containsKey(item)) {
            itemFlags.put(item, new HashSet<>());
        }
        itemFlags.get(item).addAll(flags);
    }

    /** Variante var-args di {@link #addItemFlags(Item, Collection)}. */
    public void addItemFlags(Item item, GameFlag... flag) {
        addItemFlags(item, Arrays.asList(flag));
    }

    /* ------------------------------------------------------------------ */

    /** Stampa il display name per log/debug. */
    @Override
    public String toString() {
        return displayName;
    }

    /* ------------------------------------------------------------------ */
    /*                            ROOM BUILDER                            */
    /* ------------------------------------------------------------------ */

    /**
     * Builder fluente per creare e configurare una {@link Room}.
     * <p>
     * Consente di aggiungere collegamenti, flag e oggetti in modo leggibile.
     * Tutti i campi obbligatori (display name e descrizioni) vengono validati
     * in fase di {@link #build()}.
     */
    public static class RoomBuilder {

        private String displayName;
        private String description;
        private String longDescription;

        private final Map<String, Room> connections;
        private final Map<String, Collection<GameFlag>> connectionFlags;
        private final Set<Item> items;
        private final Map<Item, Collection<GameFlag>> itemFlags;

        private final Room room;

        /**
         * Crea un builder partendo dal nome interno della stanza.
         *
         * @param name nome univoco
         */
        public RoomBuilder(String name) {
            this.room = new Room(name);
            this.connections = new HashMap<>();
            this.connectionFlags = new HashMap<>();
            this.items = new HashSet<>();
            this.itemFlags = new HashMap<>();
        }

        /**
         * Finalizza la costruzione validando i campi obbligatori
         * e restituendo la stanza configurata.
         *
         * @return istanza di {@link Room}
         * @throws NullPointerException se uno dei campi richiesti è {@code null}
         */
        public Room build() {
            Objects.requireNonNull(displayName, "The display name cannot be null");
            Objects.requireNonNull(description, "The description cannot be null");
            Objects.requireNonNull(longDescription, "The longDescription cannot be null");

            this.room.setDisplayName(this.displayName);
            this.room.setDescription(this.description);
            this.room.setLongDescription(this.longDescription);

            this.room.setConnections(connections);
            this.room.setConnectionFlags(connectionFlags);
            this.room.setItems(items);
            this.room.setItemFlags(itemFlags);

            return room;
        }

        /** Imposta il nome visualizzato. */
        public RoomBuilder setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /** Imposta la descrizione breve. */
        public RoomBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        /** Imposta la descrizione estesa. */
        public RoomBuilder setLongDescription(String longDescription) {
            this.longDescription = longDescription;
            return this;
        }

        /**
         * Collega bidirezionalmente questa stanza con {@code room}.
         *
         * @param dir         direzione da questa stanza verso {@code room}
         * @param oppositeDir direzione inversa
         * @param room        stanza da collegare
         */
        public RoomBuilder connectRoom(String dir, String oppositeDir, Room room) {
            connections.put(dir, room);
            room.connectRoom(oppositeDir, this.room);
            return this;
        }

        /** Aggiunge flag di requisito a un’uscita. */
        public RoomBuilder addConnectionFlags(String dir, Collection<GameFlag> flags) {
            if (!connectionFlags.containsKey(dir)) {
                connectionFlags.put(dir, new HashSet<>());
            }
            connectionFlags.get(dir).addAll(flags);
            return this;
        }

        /** Variante var-args di {@link #addConnectionFlags(String, Collection)}. */
        public RoomBuilder addConnectionFlags(String dir, GameFlag... flag) {
            addConnectionFlags(dir, Arrays.asList(flag));
            return this;
        }

        /** Aggiunge un oggetto alla stanza. */
        public RoomBuilder addItem(Item item) {
            items.add(item);
            return this;
        }

        /** Aggiunge flag all’oggetto. */
        public RoomBuilder addItemFlags(Item item, Collection<GameFlag> flags) {
            if (!itemFlags.containsKey(item)) {
                itemFlags.put(item, new HashSet<>());
            }
            itemFlags.get(item).addAll(flags);
            return this;
        }

        /** Variante var-args di {@link #addItemFlags(Item, Collection)}. */
        public RoomBuilder addItemFlags(Item item, GameFlag... flags) {
            return addItemFlags(item, Arrays.asList(flags));
        }

        /* ------------------- Helper “locked room” ------------------- */

        public RoomBuilder addLockedRoom(String dir, String oppositeDir, Room room, Collection<GameFlag> flags) {
            return connectRoom(dir, oppositeDir, room).addConnectionFlags(dir, flags);
        }

        public RoomBuilder addLockedRoom(String dir, String oppositeDir, Room room, GameFlag... flags) {
            return addLockedRoom(dir, oppositeDir, room, Arrays.asList(flags));
        }

        /* ------------------- Comandi rapidi cardinali --------------- */

        public RoomBuilder addNorthRoom(Room room) {
            return connectRoom("north", "south", room);
        }

        public RoomBuilder addSouthRoom(Room room) {
            return connectRoom("south", "north", room);
        }

        public RoomBuilder addEastRoom(Room room) {
            return connectRoom("east", "west", room);
        }

        public RoomBuilder addWestRoom(Room room) {
            return connectRoom("west", "east", room);
        }

        /* ------------------- Versioni “locked” cardinali ------------ */

        public RoomBuilder addLockedNorthRoom(Room room, Collection<GameFlag> flags) {
            return addLockedRoom("north", "south", room, flags);
        }

        public RoomBuilder addLockedNorthRoom(Room room, GameFlag... flags) {
            return addLockedNorthRoom(room, Arrays.asList(flags));
        }

        public RoomBuilder addLockedSouthRoom(Room room, Collection<GameFlag> flags) {
            return addLockedRoom("south", "north", room, flags);
        }

        public RoomBuilder addLockedSouthRoom(Room room, GameFlag... flags) {
            return addLockedSouthRoom(room, Arrays.asList(flags));
        }

        public RoomBuilder addLockedEastRoom(Room room, Collection<GameFlag> flags) {
            return addLockedRoom("east", "west", room, flags);
        }

        public RoomBuilder addLockedEastRoom(Room room, GameFlag... flags) {
            return addLockedEastRoom(room, Arrays.asList(flags));
        }

        public RoomBuilder addLockedWestRoom(Room room, Collection<GameFlag> flags) {
            return addLockedRoom("west", "east", room, flags);
        }

        public RoomBuilder addLockedWestRoom(Room room, GameFlag... flags) {
            return addLockedWestRoom(room, Arrays.asList(flags));
        }

        /* ------------------ Helper per oggetti nascosti ------------- */

        public RoomBuilder addHiddenItem(Item item, Collection<GameFlag> flags) {
            return addItem(item).addItemFlags(item, flags);
        }

        public RoomBuilder addHiddenItem(Item item, GameFlag... flag) {
            return addHiddenItem(item, Arrays.asList(flag));
        }
    }
}
