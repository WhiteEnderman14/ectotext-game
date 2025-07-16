package org.javamale.ectotext.server.persistence.dao;

import org.javamale.ectotext.common.model.Item;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Interfaccia InventoryDAO.
 * <p>
 * Responsabilità principale di InventoryDAO: gestire la persistenza dell'inventario degli oggetti
 * di ciascun personaggio all'interno dello stato della partita.
 * </p>
 */
public interface InventoryDAO {

    /**
     * Inserisce o aggiorna un oggetto dell'inventario associato a un personaggio e a uno stato di gioco.
     *
     * @param gameStateID    Identificativo univoco dello stato di gioco.
     * @param characterName  Nome del personaggio.
     * @param item           Oggetto da aggiungere o aggiornare nell'inventario.
     * @throws SQLException  In caso di errore durante l’accesso al database.
     */
    void merge(UUID gameStateID, String characterName, Item item) throws SQLException;

    /**
     * Rimuove un oggetto dell'inventario associato a un personaggio e a uno stato di gioco.
     *
     * @param gameStateID    Identificativo univoco dello stato di gioco.
     * @param characterName  Nome del personaggio.
     * @param item           Oggetto da rimuovere dall'inventario.
     * @throws SQLException  In caso di errore durante l’accesso al database.
     */
    void delete(UUID gameStateID, String characterName, Item item) throws SQLException;

    /**
     * Restituisce la lista di oggetti nell'inventario di un personaggio per uno specifico stato di gioco.
     *
     * @param gameStateID    Identificativo univoco dello stato di gioco.
     * @param characterName  Nome del personaggio.
     * @return Lista di oggetti Item presenti nell’inventario.
     * @throws SQLException  In caso di errore durante l’accesso al database.
     */
    List<Item> getAll(UUID gameStateID, String characterName) throws SQLException;
}
