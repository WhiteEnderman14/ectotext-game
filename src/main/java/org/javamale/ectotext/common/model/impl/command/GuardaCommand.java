package org.javamale.ectotext.common.model.impl.command;

import org.javamale.ectotext.common.model.*;
import org.javamale.ectotext.common.model.Character;
import org.javamale.ectotext.common.packet.ErrorCode;
import org.javamale.ectotext.common.packet.impl.ErrorPacket;
import org.javamale.ectotext.common.packet.impl.GameNarratorPacket;

import java.util.*;

/**
 * Classe GuardaCommand.
 * <p>
 * Responsabilità principale di GuardaCommand: gestisce il comando che permette al giocatore
 * di osservare l’ambiente circostante e ottenere la descrizione estesa della stanza corrente.
 * </p>
 */
public class GuardaCommand implements CommandHandler {
    /**
     * Mappa di alias per il bersaglio del comando.
     * Le chiavi rappresentano varianti dell’argomento testuale inserito dal giocatore,
     * i valori rappresentano il tipo di bersaglio normalizzato (es: "room").
     */
    private static final Map<String, String> targetAliases = Map.ofEntries(
            Map.entry("intorno", "room"), Map.entry("dintorni", "room"),
            Map.entry("statua", "statue"), Map.entry("cherubino", "statue"),
            Map.entry("carrello", "trolley"), Map.entry("pulizie", "trolley")
    );

    /**
     * Restituisce la descrizione estesa della stanza corrente.
     *
     * @param character   personaggio che esegue il comando
     * @param currentRoom stanza attuale del personaggio
     * @return lista contenente una risposta {@link CommandResponse} con il testo narrativo della stanza,
     * oppure un messaggio di errore se il personaggio non si trova in nessuna stanza
     */
    private List<CommandResponse> lookRoom(Character character, Room currentRoom){
        if (currentRoom == null) {
            return List.of(new CommandResponse(new GameNarratorPacket("Sei in un posto sconosciuto."), false));
        }

        return List.of(new CommandResponse(new GameNarratorPacket(currentRoom.getLongDescription()), false));
    }

    /**
     * Esegue il comando "guarda".
     * <p>
     * Identifica il bersaglio specificato negli argomenti, normalizzandolo tramite {@link #targetAliases},
     * e invoca la logica di osservazione per la stanza corrente.
     * </p>
     *
     * @param character       personaggio che invoca il comando
     * @param gameDescription descrizione globale del gioco
     * @param gameState       stato corrente del gioco
     * @param args            argomenti opzionali per specificare cosa osservare
     * @return lista di {@link CommandResponse} con il risultato della descrizione
     */
    @Override
    public List<CommandResponse> execute(Character character, GameDescription gameDescription, GameState gameState, String[] args) {
        Optional<String> optionalTarget = Arrays.stream(args)
                .filter(arg -> targetAliases.containsKey(arg.toLowerCase()))
                .map(arg -> targetAliases.get(arg.toLowerCase()))
                .findFirst();
        String target = optionalTarget.orElse("room");

        Room currentRoom = character.getCurrentRoom();

        if (currentRoom == null) {
            return List.of(new CommandResponse(
                    new ErrorPacket(ErrorCode.COMMAND_NOT_AVAILABLE, "Non sei in una stanza."),
                    false
            ));
        }


        List<CommandResponse> responses = new ArrayList<>();

        switch (target){
            case "room" -> {
                responses.addAll(lookRoom(character, character.getCurrentRoom()));
            }
            case "statue" -> {
                if(!currentRoom.getName().equals("hallway_12_nw")) {
                    responses.addAll(lookRoom(character, character.getCurrentRoom()));
                    break;
                }
                responses.add(new CommandResponse(
                        new GameNarratorPacket("La statua è un cherubino dall’espressione vagamente colpevole, appollaiato su un piedistallo di marmo scheggiato. " +
                                               "Dalla sua base cola una scia di melma verdognola che si raccoglie ai suoi piedi, nascondendo qualcosa di luccicante tra i grumi appiccicosi. " +
                                               "L’aria intorno odora di disinfettante… e di guai."),
                        true
                ));
            }
            case "trolley" -> {
                if(!currentRoom.getName().equals("hallway_12_elevator")) {
                    responses.addAll(lookRoom(character, character.getCurrentRoom()));
                    break;
                }
                responses.add(new CommandResponse(
                        new GameNarratorPacket("Carta igienica bruciata e un odore di abbrustolito. " +
                                               "Sembra tutto inutilizzabile tranne che per un MOCIO. Puoi raccoglierlo."),
                        true
                ));
            }
        }

        return responses;
    }
}
