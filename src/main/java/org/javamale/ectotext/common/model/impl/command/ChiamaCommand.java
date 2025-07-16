package org.javamale.ectotext.common.model.impl.command;

import org.javamale.ectotext.common.model.*;
import org.javamale.ectotext.common.model.Character;
import org.javamale.ectotext.common.packet.ErrorCode;
import org.javamale.ectotext.common.packet.impl.ErrorPacket;
import org.javamale.ectotext.common.packet.impl.GameNarratorPacket;

import java.util.List;

/**
 * Classe ChiamaCommand.
 * <p>
 * Responsabilità principale di ChiamaCommand: gestisce il comando che permette al giocatore
 * di chiamare l’ascensore, se si trova nella sala degli ascensori o nel corridoio degli ascensori al 12° piano.
 * Se il comando viene eseguito altrove, restituisce un errore.
 * </p>
 */
public class ChiamaCommand implements CommandHandler {

    /**
     * Esegue il comando "chiama".
     * <p>
     * Verifica se il personaggio si trova in una delle stanze abilitate per chiamare l’ascensore.
     * Se sì, aggiunge il flag {@link GameFlag#ELEVATOR_OPEN} allo stato di gioco e restituisce
     * un messaggio narrativo che indica l’apertura della porta dell’ascensore.
     * Altrimenti, restituisce una risposta di errore.
     * </p>
     *
     * @param character       personaggio che invoca il comando
     * @param gameDescription descrizione globale del gioco
     * @param gameState       stato corrente del gioco
     * @param args            eventuali argomenti (ignorati in questo comando)
     * @return lista di {@link CommandResponse} contenente una risposta di successo o di errore
     */
    @Override
    public List<CommandResponse> execute(Character character, GameDescription gameDescription, GameState gameState, String[] args) {
        if (!character.getCurrentRoom().getName().equals("hall_elevator") &&
                !character.getCurrentRoom().getName().equals("hallway_12_elevator")) {
            return List.of(new CommandResponse(
                    new ErrorPacket(ErrorCode.COMMAND_NOT_AVAILABLE),
                    false
            ));
        }

        gameState.addFlag(GameFlag.ELEVATOR_OPEN);

        return List.of(new CommandResponse(new GameNarratorPacket("Si apre la porta dell'ascensore avanti a te"), false));
    }
}
