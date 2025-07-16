package org.javamale.ectotext.server.persistence.dao.impl;

import org.javamale.ectotext.common.model.Character;
import org.javamale.ectotext.common.model.GameFlag;
import org.javamale.ectotext.common.model.GameState;
import org.javamale.ectotext.common.model.NPC;
import org.javamale.ectotext.server.persistence.dao.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Implementazione di GameStateDAO per la gestione dello stato della partita sul database.
 * Gestisce la persistenza e il recupero di flag, personaggi e NPC associati ad uno stato di gioco.
 */
public class GameStateDAOImpl implements GameStateDAO {
    /**
     * Connessione al database.
     */
    private final Connection con;

    /**
     * DAO per la gestione dei flag di gioco.
     */
    private final GameFlagDAO gameFlagDAO;

    /**
     * DAO per la gestione dei personaggi.
     */
    private final CharacterDAO characterDAO;

    /**
     * DAO per la gestione degli NPC.
     */
    private final NPCDAO npcDAO;

    /**
     * Costruisce un nuovo GameStateDAOImpl.
     *
     * @param connection    la connessione da usare per il DB
     * @param gameFlagDAO   DAO per i flag di gioco
     * @param characterDAO  DAO per i personaggi
     * @param npcDAO        DAO per gli NPC
     */
    public GameStateDAOImpl(Connection connection, GameFlagDAO gameFlagDAO, CharacterDAO characterDAO, NPCDAO npcDAO) {
        this.con = connection;
        this.gameFlagDAO = gameFlagDAO;
        this.characterDAO = characterDAO;
        this.npcDAO = npcDAO;
    }

    /**
     * Salva lo stato di gioco per una determinata stanza, gestendo aggiornamento di flag, personaggi e NPC.
     *
     * @param gameRoomName nome della stanza di gioco
     * @param gameState    stato della partita da salvare
     * @throws SQLException in caso di errori SQL
     */
    @Override
    public void save(String gameRoomName, GameState gameState) throws SQLException {
        String sql = "merge into GAME_STATES(id, gr_name) values (?, ?)";

        boolean autoCommit = con.getAutoCommit();
        con.setAutoCommit(false);

        try {
            UUID id = gameState.getUuid();

            GameState previous = get(gameRoomName);

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setObject(1, id);
                ps.setString(2, gameRoomName);
                ps.executeUpdate();
            }

            // Rimuovi elementi che non sono più presenti nello stato aggiornato
            if (previous != null) {
                for (GameFlag f : previous.getFlags()) {
                    if (!gameState.hasFlag(f)) {
                        gameFlagDAO.delete(id, f);
                    }
                }

                for (Character c : previous.getCharacters()) {
                    if (!gameState.hasCharacter(c)) {
                        characterDAO.delete(id, c);
                    }
                }

                for (NPC n : previous.getNPCs()) {
                    if (!gameState.hasNPC(n)) {
                        npcDAO.delete(id, n);
                    }
                }
            }

            // Inserisci/aggiorna i dati correnti
            for (GameFlag f : gameState.getFlags()) {
                gameFlagDAO.merge(id, f);
            }

            for (Character c : gameState.getCharacters()) {
                characterDAO.merge(id, c);
            }

            for (NPC n : gameState.getNPCs()) {
                npcDAO.merge(id, n);
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
     * Elimina lo stato di gioco dal database.
     *
     * @param gameState stato della partita da eliminare
     * @throws SQLException in caso di errori SQL
     */
    @Override
    public void delete(GameState gameState) throws SQLException {
        String sql = "delete from GAME_STATES where id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, gameState.getUuid());
            ps.executeUpdate();
        }
    }

    /**
     * Recupera lo stato di gioco associato ad una specifica stanza di gioco.
     *
     * @param gameRoomName nome della stanza di gioco
     * @return oggetto GameState trovato oppure null se non esiste
     * @throws SQLException in caso di errori SQL
     */
    @Override
    public GameState get(String gameRoomName) throws SQLException {
        String sql = "select id from GAME_STATES where gr_name = ?";

        GameState gameState = null;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, gameRoomName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UUID gameStateID = (UUID) rs.getObject("id");

                    gameState = new GameState(gameStateID);

                    gameFlagDAO.getAll(gameStateID).forEach(gameState::addFlag);
                    characterDAO.getAll(gameStateID).forEach(gameState::addCharacter);
                    npcDAO.getAll(gameStateID).forEach(gameState::addNPC);
                }
            }
        }

        return gameState;
    }
}
