package org.javamale.ectotext.common.model;

import java.util.Objects;

/**
 * Rappresenta un’entità generica presente nel mondo di gioco.
 * <p>
 * Un’{@code Entity} è identificata da:
 * <ul>
 *   <li>un <strong>nome univoco</strong> (campo {@link #name});</li>
 *   <li>la <strong>stanza</strong> in cui si trova attualmente
 *       ({@link #currentRoom}).</li>
 * </ul>
 * Viene estesa da classi concrete come {@code Character}, {@code Item},
 * {@code NPC}, ecc., che aggiungono comportamento e dati specifici.
 */
public class Entity {

    /** Identificatore univoco leggibile (es. “chiave_ferro”, “caverna”). */
    protected final String name;

    /** Stanza in cui l’entità è posizionata in questo momento. */
    protected Room currentRoom;

    /**
     * Costruisce una nuova entità.
     *
     * @param name        nome univoco dell’entità
     * @param currentRoom stanza iniziale in cui collocare l’entità
     */
    public Entity(String name, Room currentRoom) {
        this.name = name;
        this.currentRoom = currentRoom;
    }

    /**
     * Restituisce il nome dell’entità.
     *
     * @return nome univoco
     */
    public String getName() {
        return name;
    }

    /**
     * Restituisce la stanza attuale in cui si trova l’entità.
     *
     * @return stanza corrente
     */
    public Room getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Sposta l’entità nella stanza specificata.
     *
     * @param nextRoom nuova stanza in cui collocare l’entità
     */
    public void move(Room nextRoom) {
        this.currentRoom = nextRoom;
    }

    // ---------------------------------------------------------------------
    //                    METODI DI UTILITÀ / OVERRIDE
    // ---------------------------------------------------------------------

    /** Due entità sono uguali se hanno lo stesso {@link #name}. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(name, entity.name);
    }

    /** Calcolato unicamente sul campo {@link #name}. */
    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    /** Restituisce il nome: utile per log, debug e stampa in chiaro. */
    @Override
    public String toString() {
        return name;
    }
}
