package org.javamale.ectotext.common.model;

import java.util.Objects;

/**
 * Rappresenta un <em>oggetto di gioco</em> che può essere raccolto,
 * tenuto nell’inventario e utilizzato dal giocatore o dagli NPC.
 * <p>
 * Ogni {@code Item} è descritto da:
 * <ul>
 *   <li>un <strong>nome interno</strong> univoco ({@link #name}) usato per
 *       logica e confronti;</li>
 *   <li>un <strong>nome visualizzato</strong> ({@link #displayName})
 *       mostrato nelle interfacce testuali/grafiche;</li>
 *   <li>una <strong>descrizione</strong> testuale
 *       ({@link #description}) letta dai giocatori.</li>
 * </ul>
 */
public class Item {

    /** Identificatore univoco dell’oggetto, immutabile. */
    private final String name;

    /** Nome leggibile dall’utente. */
    private String displayName;

    /** Descrizione testuale dell’oggetto. */
    private String description;

    /**
     * Costruisce un nuovo {@code Item}.
     *
     * @param name        nome interno univoco (non visibile al giocatore)
     * @param displayName nome visualizzato al giocatore
     * @param description descrizione testuale dell’oggetto
     */
    public Item(String name, String displayName, String description) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
    }

    /** @return nome interno univoco dell’oggetto */
    public String getName() {
        return name;
    }

    /** @return nome visualizzato al giocatore */
    public String getDisplayName() {
        return displayName;
    }

    /** @return descrizione testuale dell’oggetto */
    public String getDescription() {
        return description;
    }

    /**
     * Aggiorna il nome visualizzato.
     *
     * @param displayName nuovo display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Aggiorna la descrizione testuale.
     *
     * @param description nuova descrizione
     */
    public void setDescription(String description) {
        this.description = description;
    }

    // ---------------------------------------------------------------------
    //                      METODI DI UTILITÀ / OVERRIDE
    // ---------------------------------------------------------------------

    /**
     * Due oggetti sono considerati uguali se hanno lo stesso
     * {@link #name} interno (case-sensitive).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item item)) return false;
        return name.equals(item.name);
    }

    /** Hash basato esclusivamente sul campo {@link #name}. */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /** Restituisce il nome visualizzato, utile per stampa e log. */
    @Override
    public String toString() {
        return displayName;
    }
}
