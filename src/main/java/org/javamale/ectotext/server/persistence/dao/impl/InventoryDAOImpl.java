package org.javamale.ectotext.server.persistence.dao.impl;

import org.javamale.ectotext.common.model.Item;
import org.javamale.ectotext.server.persistence.dao.InventoryDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementazione DAO per la gestione dell'inventario dei personaggi all'interno di uno stato di gioco.
 * Permette di salvare, eliminare e recuperare gli oggetti posseduti dai personaggi nel database.
 */
public class InventoryDAOImpl implements InventoryDAO {
    /**
     * Connessione al database.
     */
    private final Connection con;

    /**
     * Costruisce un nuovo InventoryDAOImpl.
     * @param con connessione da usare per le operazioni sul database.
     */
    public InventoryDAOImpl(Connection con) {
        this.con = con;
    }

    /**
     * Inserisce o aggiorna un oggetto nell'inventario di un personaggio per uno specifico stato di gioco.
     * Se già presente aggiorna i dati, altrimenti lo inserisce.
     *
     * @param gameStateID    identificatore univoco dello stato di gioco
     * @param characterName  nome del personaggio
     * @param item           oggetto da inserire o aggiornare nell'inventario
     * @throws SQLException  in caso di errori SQL
     */
    @Override
    public void merge(UUID gameStateID, String characterName, Item item) throws SQLException {
        String sql = """
            merge into character_inventories (
                game_state_id, character_name, item_name, item_display_name, item_description
            ) values (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, gameStateID);
            ps.setString(2, characterName);
            ps.setString(3, item.getName());
            ps.setString(4, item.getDisplayName());
            ps.setString(5, item.getDescription());
            ps.executeUpdate();
        }
    }

    /**
     * Rimuove un oggetto dall'inventario di un personaggio.
     *
     * @param gameStateID    identificatore dello stato di gioco
     * @param characterName  nome del personaggio
     * @param item           oggetto da eliminare dall'inventario
     * @throws SQLException  in caso di errori SQL
     */
    @Override
    public void delete(UUID gameStateID, String characterName, Item item) throws SQLException {
        String sql = """
            delete from character_inventories
            where game_state_id = ? and character_name = ? and item_name = ?
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, gameStateID);
            ps.setString(2, characterName);
            ps.setString(3, item.getName());
            ps.executeUpdate();
        }
    }

    /**
     * Recupera tutti gli oggetti nell'inventario di un personaggio per uno specifico stato di gioco.
     *
     * @param gameStateID    identificatore dello stato di gioco
     * @param characterName  nome del personaggio
     * @return lista degli oggetti posseduti dal personaggio
     * @throws SQLException  in caso di errori SQL
     */
    @Override
    public List<Item> getAll(UUID gameStateID, String characterName) throws SQLException {
        String sql = """
            SELECT item_name, item_display_name, item_description
            FROM character_inventories
            WHERE game_state_id = ? AND character_name = ?
        """;

        List<Item> items = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, gameStateID);
            ps.setString(2, characterName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("item_name");
                    String displayName = rs.getString("item_display_name");
                    String description = rs.getString("item_description");
                    items.add(new Item(name, displayName, description));
                }
            }
        }

        return items;
    }
}
