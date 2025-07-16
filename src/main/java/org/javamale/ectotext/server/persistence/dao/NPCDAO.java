package org.javamale.ectotext.server.persistence.dao;

import org.javamale.ectotext.common.model.NPC;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Interfaccia NPCDAO.
 * <p>
 * Responsabilità principale di NPCDAO: gestire la persistenza degli NPC (personaggi non giocanti)
 * associati allo stato della partita.
 * </p>
 */
public interface NPCDAO {

    /**
     * Inserisce o aggiorna un NPC associato a uno specifico stato di gioco.
     *
     * @param gameStateID  Identificativo univoco dello stato di gioco.
     * @param npc          NPC da inserire o aggiornare.
     * @throws SQLException In caso di errore durante le operazioni sul database.
     */
    void merge(UUID gameStateID, NPC npc) throws SQLException;

    /**
     * Elimina un NPC associato a uno specifico stato di gioco.
     *
     * @param gameStateID  Identificativo univoco dello stato di gioco.
     * @param npc          NPC da eliminare.
     * @throws SQLException In caso di errore durante le operazioni sul database.
     */
    void delete(UUID gameStateID, NPC npc) throws SQLException;

    /**
     * Restituisce tutti gli NPC associati a uno specifico stato di gioco.
     *
     * @param gameStateID  Identificativo univoco dello stato di gioco.
     * @return Lista degli NPC presenti nello stato di gioco indicato.
     * @throws SQLException In caso di errore durante le operazioni sul database.
     */
    List<NPC> getAll(UUID gameStateID) throws SQLException;
}
