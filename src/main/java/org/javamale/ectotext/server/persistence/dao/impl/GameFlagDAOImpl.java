package org.javamale.ectotext.server.persistence.dao.impl;

import org.javamale.ectotext.common.model.GameFlag;
import org.javamale.ectotext.server.persistence.dao.GameFlagDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Classe GameFlagDAOImpl.
 * <p>
 * Responsabilità principale di GameFlagDAOImpl: implementazione concreta dell’interfaccia
 * GameFlagDAO per la gestione della persistenza dei flag di gioco (GameFlag) su database.
 * Permette di aggiungere, rimuovere e recuperare flag associati a uno stato di gioco.
 * </p>
 */
public class GameFlagDAOImpl implements GameFlagDAO {
    /**
     * Connessione al database.
     */
    private final Connection con;

    /**
     * Costruttore.
     *
     * @param con connessione al database
     */
    public GameFlagDAOImpl(Connection con) {
        this.con = con;
    }

    /**
     * Inserisce o aggiorna (upsert) un flag di gioco associato a uno stato di gioco.
     *
     * @param gameStateID identificatore dello stato di gioco (UUID)
     * @param gameFlag    flag di gioco da aggiungere o aggiornare
     * @throws SQLException in caso di errore SQL
     */
    @Override
    public void merge(UUID gameStateID, GameFlag gameFlag) throws SQLException {
        String sql = "merge into game_flags (game_state_id, flag_key) values (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, gameStateID);
            ps.setString(2, gameFlag.getKey());
            ps.executeUpdate();
        }
    }

    /**
     * Rimuove un flag di gioco associato a uno stato di gioco.
     *
     * @param gameStateID identificatore dello stato di gioco (UUID)
     * @param gameFlag    flag di gioco da rimuovere
     * @throws SQLException in caso di errore SQL
     */
    @Override
    public void delete(UUID gameStateID, GameFlag gameFlag) throws SQLException {
        String sql = "delete from game_flags where game_state_id = ? and flag_key = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, gameStateID);
            ps.setString(2, gameFlag.getKey());
            ps.executeUpdate();
        }
    }

    /**
     * Recupera tutti i flag di gioco associati a uno stato di gioco.
     *
     * @param gameStateID identificatore dello stato di gioco (UUID)
     * @return lista di flag di gioco (GameFlag)
     * @throws SQLException in caso di errore SQL
     */
    @Override
    public List<GameFlag> getAll(UUID gameStateID) throws SQLException {
        String sql = "select flag_key from game_flags where game_state_id = ?";
        List<GameFlag> flags = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, gameStateID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString("flag_key");
                    GameFlag flag = GameFlag.fromKey(key);
                    if (flag != null) {
                        flags.add(flag);
                    } else {
                        throw new IllegalStateException("Unexpected key '" + key + "'");
                    }
                }
            }
        }

        return flags;
    }
}
