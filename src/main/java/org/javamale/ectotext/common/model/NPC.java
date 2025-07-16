package org.javamale.ectotext.common.model;

/**
 * Rappresenta un <em>personaggio non giocante</em> (NPC) presente nel mondo.
 * <p>
 * Un {@code NPC} condivide la struttura base di {@link Entity} e possiede
 * un <strong>display name</strong> leggibile dai giocatori, usato per
 * dialoghi, descrizioni e messaggi a schermo.
 */
public class NPC extends Entity {

    /** Nome visualizzato nei messaggi di gioco. */
    protected String displayName;

    /**
     * Costruisce un nuovo NPC.
     *
     * @param name         nome interno univoco dell’NPC
     * @param displayName  nome visualizzato al giocatore
     * @param startingRoom stanza iniziale in cui appare l’NPC
     */
    public NPC(String name, String displayName, Room startingRoom) {
        super(name, startingRoom);
        this.displayName = displayName;
    }

    /** @return nome visualizzato dell’NPC */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Aggiorna il nome visualizzato dell’NPC.
     *
     * @param displayName nuovo display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
