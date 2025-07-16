package org.javamale.ectotext.server.persistence.dao.impl;

import org.javamale.ectotext.common.model.GameDescription;
import org.javamale.ectotext.common.model.NPC;
import org.javamale.ectotext.common.model.Room;
import org.javamale.ectotext.server.persistence.dao.NPCDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementazione concreta dell'interfaccia {@link NPCDAO} per la gestione degli NPC nel database.
 * Responsabilità: inserimento, cancellazione e recupero degli NPC legati allo stato di gioco.
 */
public class NPCDAOImpl implements NPCDAO {
    /**
     * Connessione al database.
     */
    private final Connection con;

    /**
     * Descrizione del gioco, utile per la mappatura delle stanze.
     */
    private final GameDescription gameDescription;

    /**
     * Costruisce un nuovo oggetto NPCDAOImpl.
     * @param con             Connessione al database da utilizzare.
     * @param gameDescription Descrizione del gioco corrente.
     */
    public NPCDAOImpl(Connection con, GameDescription gameDescription) {
        this.con = con;
        this.gameDescription = gameDescription;
    }

    /**
     * Inserisce o aggiorna un NPC nel database per uno specifico stato di gioco.
     *
     * @param gameStateID Identificativo univoco dello stato di gioco.
     * @param npc         Oggetto NPC da salvare o aggiornare.
     * @throws SQLException in caso di errori SQL.
     */
    @Override
    public void merge(UUID gameStateID, NPC npc) throws SQLException {
        String sql = """
            merge into npcs (game_state_id, npc_name, npc_display_name, npc_current_room)
            values (?, ?, ?, ?)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, gameStateID);
            ps.setString(2, npc.getName());
            ps.setString(3, npc.getDisplayName());
            ps.setString(4, npc.getCurrentRoom().getName());
            ps.executeUpdate();
        }
    }

    /**
     * Cancella un NPC dal database relativo a uno stato di gioco specifico.
     *
     * @param gameStateID Identificativo dello stato di gioco.
     * @param npc         Oggetto NPC da eliminare.
     * @throws SQLException in caso di errori SQL.
     */
    @Override
    public void delete(UUID gameStateID, NPC npc) throws SQLException {
        String sql = "delete from npcs where game_state_id = ? and npc_name = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, gameStateID);
            ps.setString(2, npc.getName());
            ps.executeUpdate();
        }
    }

    /**
     * Restituisce la lista di tutti gli NPC relativi a uno specifico stato di gioco.
     *
     * @param gameStateID Identificativo dello stato di gioco.
     * @return Lista degli oggetti NPC trovati.
     * @throws SQLException in caso di errori SQL.
     */
    @Override
    public List<NPC> getAll(UUID gameStateID) throws SQLException {
        String sql = "select npc_name, npc_display_name, npc_current_room from npcs where game_state_id = ?";

        List<NPC> npcs = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, gameStateID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("npc_name");
                    String displayName = rs.getString("npc_display_name");
                    String roomName = rs.getString("npc_current_room");

                    Room room = gameDescription.getGameMap().getRoom(roomName);
                    npcs.add(new NPC(name, displayName, room));
                }
            }
        }

        return npcs;
    }
}
