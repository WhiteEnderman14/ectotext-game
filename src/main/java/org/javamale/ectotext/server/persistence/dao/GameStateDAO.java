package org.javamale.ectotext.server.persistence.dao;

import org.javamale.ectotext.common.model.GameState;

import java.sql.SQLException;

/**
 * Interfaccia GameStateDAO.
 * <p>
 * Responsabilità principale di GameStateDAO: gestire la persistenza dello stato della partita ({@link GameState})
 * associato a una stanza di gioco nel database.
 * </p>
 */
public interface GameStateDAO {

    /**
     * Salva lo stato della partita nel database associandolo a una stanza di gioco.
     *
     * @param gameRoomName Nome della stanza di gioco.
     * @param gameState    Oggetto GameState da salvare.
     * @throws SQLException in caso di errori di accesso al database.
     */
    void save(String gameRoomName, GameState gameState) throws SQLException;

    /**
     * Elimina lo stato della partita dal database.
     *
     * @param gameState Oggetto GameState da eliminare.
     * @throws SQLException in caso di errori di accesso al database.
     */
    void delete(GameState gameState) throws SQLException;

    /**
     * Recupera lo stato della partita associato a una stanza di gioco.
     *
     * @param gameRoomName Nome della stanza di gioco.
     * @return Oggetto GameState associato, o null se non trovato.
     * @throws SQLException in caso di errori di accesso al database.
     */
    GameState get(String gameRoomName) throws SQLException;
}
