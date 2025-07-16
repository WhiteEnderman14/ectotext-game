package org.javamale.ectotext.server.persistence.dao;

import org.javamale.ectotext.common.model.GameFlag;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Interfaccia GameFlagDAO.
 * <p>
 * Responsabilità principale di GameFlagDAO: definire le operazioni CRUD per la persistenza
 * degli oggetti {@link GameFlag} associati a uno stato di gioco nel database.
 * </p>
 */
public interface GameFlagDAO {

    /**
     * Inserisce o aggiorna un oggetto GameFlag associato a uno specifico stato di gioco.
     *
     * @param gameStateID Identificativo univoco dello stato di gioco.
     * @param gameFlag    Oggetto GameFlag da inserire o aggiornare.
     * @throws SQLException in caso di errori di accesso al database.
     */
    void merge(UUID gameStateID, GameFlag gameFlag) throws SQLException;

    /**
     * Elimina un oggetto GameFlag associato a uno stato di gioco.
     *
     * @param gameStateID Identificativo univoco dello stato di gioco.
     * @param gameFlag    Oggetto GameFlag da eliminare.
     * @throws SQLException in caso di errori di accesso al database.
     */
    void delete(UUID gameStateID, GameFlag gameFlag) throws SQLException;

    /**
     * Restituisce la lista di tutti i GameFlag associati a uno stato di gioco.
     *
     * @param gameStateID Identificativo univoco dello stato di gioco.
     * @return Lista di oggetti GameFlag relativi allo stato di gioco specificato.
     * @throws SQLException in caso di errori di accesso al database.
     */
    List<GameFlag> getAll(UUID gameStateID) throws SQLException;
}
