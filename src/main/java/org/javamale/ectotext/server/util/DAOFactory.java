package org.javamale.ectotext.server.util;

import org.javamale.ectotext.common.model.GameDescription;
import org.javamale.ectotext.server.persistence.dao.CharacterDAO;
import org.javamale.ectotext.server.persistence.dao.GameFlagDAO;
import org.javamale.ectotext.server.persistence.dao.InventoryDAO;
import org.javamale.ectotext.server.persistence.dao.NPCDAO;
import org.javamale.ectotext.server.persistence.dao.impl.*;

import java.sql.Connection;

/**
 * Classe DAOFactory.
 * <p>
 * Responsabilità principale di DAOFactory: fornire metodi factory per la creazione delle implementazioni
 * dei vari DAO necessari alla persistenza dello stato di gioco.
 * </p>
 */
public class DAOFactory {
    /**
     * Crea e restituisce una nuova istanza di GameStateDAOImpl, inizializzando internamente
     * anche le istanze di InventoryDAO, NPCDAO, CharacterDAO e GameFlagDAO necessarie.
     *
     * @param connection      valore di tipo Connection, connessione al database.
     * @param gameDescription valore di tipo GameDescription, descrizione del gioco.
     * @return un'istanza di GameStateDAOImpl.
     */
    public static GameStateDAOImpl createGameStateDAO(Connection connection, GameDescription gameDescription) {
        InventoryDAO inventoryDAO = new InventoryDAOImpl(connection);

        NPCDAO npcDAO = new NPCDAOImpl(connection, gameDescription);
        CharacterDAO characterDAO = new CharacterDAOImpl(connection, inventoryDAO, gameDescription);
        GameFlagDAO gameFlagDAO = new GameFlagDAOImpl(connection);

        return new GameStateDAOImpl(connection, gameFlagDAO, characterDAO, npcDAO);
    }
}
