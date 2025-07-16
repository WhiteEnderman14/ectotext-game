package org.javamale.ectotext.server.persistence.dao;

import org.javamale.ectotext.server.core.GameRoom;

import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia GameRoomDAO.
 * <p>
 * Responsabilità principale di GameRoomDAO: gestire la persistenza delle stanze di gioco ({@link GameRoom})
 * nel database, fornendo operazioni CRUD di base.
 * </p>
 */
public interface GameRoomDAO {

    /**
     * Aggiunge una nuova stanza di gioco al database.
     *
     * @param gameRoom Oggetto GameRoom da aggiungere.
     * @throws SQLException in caso di errori di accesso al database.
     */
    void add(GameRoom gameRoom) throws SQLException;

    /**
     * Elimina una stanza di gioco dal database tramite il suo nome.
     *
     * @param roomName Nome della stanza da eliminare.
     * @throws SQLException in caso di errori di accesso al database.
     */
    void delete(String roomName) throws SQLException;

    /**
     * Restituisce una stanza di gioco dato il suo nome.
     *
     * @param roomName Nome della stanza da recuperare.
     * @return Oggetto GameRoom corrispondente al nome fornito, o null se non esiste.
     * @throws SQLException in caso di errori di accesso al database.
     */
    GameRoom get(String roomName) throws SQLException;

    /**
     * Restituisce la lista di tutte le stanze di gioco presenti nel database.
     *
     * @return Lista di oggetti GameRoom.
     * @throws SQLException in caso di errori di accesso al database.
     */
    List<GameRoom> getAll() throws SQLException;
}
