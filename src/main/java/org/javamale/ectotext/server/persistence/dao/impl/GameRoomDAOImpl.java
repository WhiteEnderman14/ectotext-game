package org.javamale.ectotext.server.persistence.dao.impl;

import org.javamale.ectotext.server.core.GameRoom;
import org.javamale.ectotext.server.persistence.dao.GameRoomDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe GameRoomDAOImpl.
 * <p>
 * Implementazione concreta dell’interfaccia {@link GameRoomDAO} per la gestione delle stanze di gioco
 * su database. Permette di aggiungere, eliminare, recuperare una singola stanza o tutte le stanze.
 * </p>
 */
public class GameRoomDAOImpl implements GameRoomDAO {

    /**
     * Connessione al database.
     */
    private final Connection con;

    /**
     * Costruisce un nuovo GameRoomDAOImpl.
     *
     * @param connection la connessione al database
     */
    public GameRoomDAOImpl(Connection connection) {
        this.con = connection;
    }

    /**
     * Aggiunge una nuova stanza di gioco al database.
     *
     * @param gameRoom oggetto GameRoom da aggiungere
     * @throws SQLException in caso di errore SQL
     */
    @Override
    public void add(GameRoom gameRoom) throws SQLException {
        String query = "insert into game_rooms(gr_name, gr_password) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, gameRoom.getName());
            ps.setString(2, gameRoom.getPassword());
            ps.executeUpdate();
        }
    }

    /**
     * Elimina una stanza di gioco dal database, identificata dal suo nome.
     *
     * @param roomName nome della stanza da eliminare
     * @throws SQLException in caso di errore SQL
     */
    @Override
    public void delete(String roomName) throws SQLException {
        String query = "delete from game_rooms where gr_name = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, roomName);
            ps.executeUpdate();
        }
    }

    /**
     * Restituisce la stanza di gioco corrispondente al nome specificato, se esistente.
     *
     * @param roomName nome della stanza da recuperare
     * @return oggetto GameRoom trovato o null se non esiste
     * @throws SQLException in caso di errore SQL
     */
    @Override
    public GameRoom get(String roomName) throws SQLException {
        String query = "select * from game_rooms where gr_name = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, roomName);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String password = rs.getString("gr_password");
                return new GameRoom(roomName, password);
            }
        }
    }

    /**
     * Restituisce una lista con tutte le stanze di gioco presenti nel database.
     *
     * @return lista di oggetti GameRoom
     * @throws SQLException in caso di errore SQL
     */
    @Override
    public List<GameRoom> getAll() throws SQLException {
        String query = "select * from game_rooms order by gr_name";
        List<GameRoom> gameRooms = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String roomName = rs.getString("gr_name");
                String password = rs.getString("gr_password");
                gameRooms.add(new GameRoom(roomName, password));
            }
        }

        return gameRooms;
    }
}
