package org.javamale.ectotext.common.model.impl.command;

import org.javamale.ectotext.common.model.*;
import org.javamale.ectotext.common.model.Character;
import org.javamale.ectotext.common.packet.ErrorCode;
import org.javamale.ectotext.common.packet.impl.ErrorPacket;
import org.javamale.ectotext.common.packet.impl.GameNarratorPacket;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Classe RaccogliCommand.
 * <p>
 * Responsabilità principale di RaccogliCommand: gestisce il comando per raccogliere
 * oggetti presenti nella stanza, aggiungendoli all'inventario del personaggio.
 * </p>
 */
public class RaccogliCommand implements CommandHandler {

    /**
     * Restituisce una risposta di errore se l’oggetto è nascosto o non ancora accessibile.
     *
     * @param character personaggio che tenta di raccogliere
     * @param item      oggetto ricercato
     * @param gameState stato corrente del gioco
     * @return lista contenente una risposta di errore {@link CommandResponse}
     */
    private List<CommandResponse> hiddenItem(Character character, Item item, GameState gameState) {
        return List.of(new CommandResponse(
                        new ErrorPacket(ErrorCode.COMMAND_WITHOUT_ARGS, "Non trovi ciò che cerchi"),
                        false
                )
        );
    }

    /**
     * Esegue il comando di raccolta oggetto.
     * <ul>
     *     <li>Cerca l’oggetto nella stanza in base agli argomenti inseriti.</li>
     *     <li>Controlla che l’oggetto sia visibile (tramite flag e checkItemFlags).</li>
     *     <li>Se già raccolto, restituisce errore.</li>
     *     <li>Altrimenti aggiunge l’oggetto all’inventario del personaggio e aggiorna lo stato di raccolta.</li>
     * </ul>
     *
     * @param character       personaggio che invoca il comando
     * @param gameDescription descrizione globale del gioco
     * @param gameState       stato corrente del gioco
     * @param args            argomenti forniti dal giocatore
     * @return lista di risposte {@link CommandResponse} (errore o successo)
     */
    @Override
    public List<CommandResponse> execute(Character character, GameDescription gameDescription, GameState gameState, String[] args) {
        Room currentRoom = character.getCurrentRoom();

        if (currentRoom == null) {
            return List.of(new CommandResponse(
                            new ErrorPacket(ErrorCode.COMMAND_NOT_AVAILABLE, "Non sei in una stanza."),
                            false
                    )
            );
        }

        Optional<Item> optionalItem = Arrays.stream(args)
                .map(arg -> currentRoom.getItems().stream()
                        .filter(item -> item.getDisplayName().toLowerCase().contains(arg.toLowerCase()))
                        .findFirst()
                )
                .flatMap(Optional::stream)
                .findFirst();

        if (optionalItem.isEmpty()) {
            return List.of(new CommandResponse(
                            new ErrorPacket(ErrorCode.COMMAND_WITHOUT_ARGS, "Non trovi ciò che cerchi"),
                            false
                    )
            );
        }

        Item item = optionalItem.get();

        if (!currentRoom.checkItemFlags(item, gameState.getFlags())){
            return hiddenItem(character, item, gameState);
        }

        if (gameState.hasFlag(GameFlag.fromKey("room." + currentRoom.getName() + ".item." + item.getName() + ".collected"))) {
            return List.of(new CommandResponse(
                            new ErrorPacket(ErrorCode.COMMAND_WITHOUT_ARGS, "Non trovi ciò che cerchi"),
                            false
                    )
            );
        }

        gameState.addFlag(GameFlag.fromKey("room." + currentRoom.getName() + ".item." + item.getName() + ".collected"));

        character.addItem(item);

        return List.of(new CommandResponse(
                new GameNarratorPacket("Hai raccolto " + item.getDisplayName()),
                false
        ));
    }
}
