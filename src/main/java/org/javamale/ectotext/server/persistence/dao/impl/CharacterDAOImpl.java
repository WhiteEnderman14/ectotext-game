package org.javamale.ectotext.server.persistence.dao.impl;

import org.javamale.ectotext.common.model.Character;
import org.javamale.ectotext.common.model.GameDescription;
import org.javamale.ectotext.common.model.Item;
import org.javamale.ectotext.common.model.Room;
import org.javamale.ectotext.server.persistence.dao.CharacterDAO;
import org.javamale.ectotext.server.persistence.dao.InventoryDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Classe CharacterDAOImpl.
 * <p>
 * Responsabilità principale di CharacterDAOImpl: implementazione concreta
 * dell’interfaccia CharacterDAO per la gestione della persistenza dei personaggi (Character)
 * nel database, inclusa la gestione dell’inventario associato.
 * </p>
 */
public class CharacterDAOImpl implements CharacterDAO {
    /**
     * Connessione al database.
     */
    private final Connection con;

    /**
     * DAO per la gestione dell'inventario dei personaggi.
     */
    private final InventoryDAO inventoryDAO;

    /**
     * Descrizione del gioco (necessaria per il mapping delle stanze).
     */
    private final GameDescription gameDescription;

    /**
     * Costruttore.
     *
     * @param con             connessione al database
     * @param inventoryDAO    DAO per gestire l'inventario del personaggio
     * @param gameDescription descrizione della mappa e delle entità del gioco
     */
    public CharacterDAOImpl(Connection con, InventoryDAO inventoryDAO, GameDescription gameDescription) {
        this.con = con;
        this.inventoryDAO = inventoryDAO;
        this.gameDescription = gameDescription;
    }

    /**
     * Salva o aggiorna un personaggio nel database, compreso l'inventario.
     *
     * @param gameStateID id dello stato di gioco
     * @param character   personaggio da salvare/aggiornare
     * @throws SQLException in caso di errore SQL
     */
    @Override
    public void merge(UUID gameStateID, Character character) throws SQLException {
        String sql = """
            merge into characters (
                game_state_id, character_name, character_display_name, character_current_room
            ) values (?, ?, ?, ?)
        """;

        boolean autoCommit = con.getAutoCommit();
        con.setAutoCommit(false);

        try {
            List<Item> previous = inventoryDAO.getAll(gameStateID, character.getName());

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setObject(1, gameStateID);
                ps.setString(2, character.getName());
                ps.setString(3, character.getDisplayName());
                ps.setString(4, character.getCurrentRoom().getName());
                ps.executeUpdate();
            }

            for (Item item : previous) {
                if(!character.hasItem(item.getName())){
                    inventoryDAO.delete(gameStateID, character.getName(), item);
                }
            }

            for (Item item : character.getInventory()) {
                inventoryDAO.merge(gameStateID, character.getName(), item);
            }

            //con.commit();

        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(autoCommit);
        }
    }

    /**
     * Elimina un personaggio dal database.
     *
     * @param gameStateID id dello stato di gioco
     * @param character   personaggio da eliminare
     * @throws SQLException in caso di errore SQL
     */
    @Override
    public void delete(UUID gameStateID, Character character) throws SQLException {
        String sql = "delete from characters where game_state_id = ? and character_name = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, gameStateID);
            ps.setString(2, character.getName());
            ps.executeUpdate();
        }
    }

    /**
     * Restituisce la lista di tutti i personaggi associati a uno stato di gioco.
     *
     * @param gameStateID id dello stato di gioco
     * @return lista di personaggi
     * @throws SQLException in caso di errore SQL
     */
    @Override
    public List<Character> getAll(UUID gameStateID) throws SQLException {
        String sql = """
            select character_name, character_display_name, character_current_room
            from characters
            where game_state_id = ?
        """;

        List<Character> characters = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, gameStateID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("character_name");
                    String displayName = rs.getString("character_display_name");
                    String roomName = rs.getString("character_current_room");

                    Room room = gameDescription.getGameMap().getRoom(roomName);
                    Character.CharacterBuilder builder = new Character.CharacterBuilder(name)
                            .setDisplayName(displayName)
                            .setStartingRoom(room);

                    List<Item> inventory = inventoryDAO.getAll(gameStateID, name);
                    builder.addItems(inventory);

                    characters.add(builder.build());
                }
            }
        }

        return characters;
    }
}
