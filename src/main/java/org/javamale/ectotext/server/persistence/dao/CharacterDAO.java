package org.javamale.ectotext.server.persistence.dao;

import org.javamale.ectotext.common.model.Character;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Interfaccia CharacterDAO.
 * <p>
 * Responsabilità principale di CharacterDAO: definire le operazioni CRUD per la persistenza
 * degli oggetti {@link Character} associati a uno stato di gioco nel database.
 * </p>
 */
public interface CharacterDAO {

    /**
     * Inserisce o aggiorna un oggetto Character associato a uno specifico stato di gioco.
     *
     * @param gameStateID Identificativo univoco dello stato di gioco.
     * @param character   Oggetto Character da inserire o aggiornare.
     * @throws SQLException in caso di errori di accesso al database.
     */
    void merge(UUID gameStateID, Character character) throws SQLException;

    /**
     * Elimina un oggetto Character associato a uno stato di gioco.
     *
     * @param gameStateID Identificativo univoco dello stato di gioco.
     * @param character   Oggetto Character da eliminare.
     * @throws SQLException in caso di errori di accesso al database.
     */
    void delete(UUID gameStateID, Character character) throws SQLException;

    /**
     * Restituisce la lista di tutti i Character associati a uno stato di gioco.
     *
     * @param gameStateID Identificativo univoco dello stato di gioco.
     * @return Lista di oggetti Character relativi allo stato di gioco specificato.
     * @throws SQLException in caso di errori di accesso al database.
     */
    List<Character> getAll(UUID gameStateID) throws SQLException;
}
