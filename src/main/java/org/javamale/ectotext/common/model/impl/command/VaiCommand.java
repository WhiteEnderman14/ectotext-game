package org.javamale.ectotext.common.model.impl.command;

import org.javamale.ectotext.common.model.*;
import org.javamale.ectotext.common.model.Character;
import org.javamale.ectotext.common.packet.ErrorCode;
import org.javamale.ectotext.common.packet.impl.ErrorPacket;
import org.javamale.ectotext.common.packet.impl.GameDialoguePacket;
import org.javamale.ectotext.common.packet.impl.GameNarratorPacket;

import java.util.*;

/**
 * <h2>Comando “vai”</h2>
 * Gestisce la logica di spostamento del personaggio in una direzione
 * specificata dall’utente.
 * <ul>
 *     <li>Controlla se la direzione è valida e gestisce eventuali alias.</li>
 *     <li>Gestisce casi speciali come stanze bloccate o uscite vietate.</li>
 *     <li>Esegue il movimento del personaggio (o di tutti, se necessario).</li>
 *     <li>Restituisce una lista di risposte da inviare al client.</li>
 * </ul>
 */
public class VaiCommand implements CommandHandler {

    /** Alias direzionali in ingresso (input utente) → direzione standard. */
    private static final Map<String, String> directionAliases = Map.ofEntries(
            Map.entry("nord", "north"), Map.entry("n", "north"),
            Map.entry("sud", "south"), Map.entry("s", "south"),
            Map.entry("est", "east"), Map.entry("e", "east"),
            Map.entry("ovest", "west"), Map.entry("o", "west"),
            Map.entry("su", "north"), Map.entry("alto", "north"),
            Map.entry("giu", "south"), Map.entry("basso", "south"),
            Map.entry("12", "12"), Map.entry("dodici", "12"), Map.entry("dodicesimo", "12"),
            Map.entry("pt", "pt"), Map.entry("t", "pt"), Map.entry("terra", "pt")
    );

    /**
     * Restituisce la risposta corretta quando il movimento porta verso una direzione che
     * non corrisponde a nessuna stanza collegata.
     *
     * <p>Esempio: tentativo di uscire dall’hotel dalla hall oppure muoversi dove non c’è nulla.</p>
     *
     * @param character   Personaggio che ha invocato il comando
     * @param currentRoom Stanza attuale del personaggio
     * @param dir         Direzione richiesta (già normalizzata)
     * @param gameState   Stato di gioco corrente
     * @return elenco di risposte {@link CommandResponse}
     */
    private List<CommandResponse> nullRoom(Character character, Room currentRoom, String dir, GameState gameState) {
        List<CommandResponse> responses = new ArrayList<>();

        if (currentRoom.getName().equals("hall") && dir.equals("south")) {
            if (!gameState.hasFlag(GameFlag.GHOST_CAPTURED)) {
                switch (character.getName()) {
                    case "peter" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), "Non possiamo andare via, non ci hanno ancora pagati"), false
                    ));
                    case "ray" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), "Non possiamo andare via, non abbiamo ancora catturato il fantasma"), false
                    ));
                    case "egon" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), "Non possiamo andare via, non ho ancora finito di studiare questo fenomeno"), false
                    ));
                }
                responses.add(new CommandResponse(
                        new GameNarratorPacket("Sei ancora nella hall dell'hotel"), false)
                );
            } else {
                responses.add(new CommandResponse(
                        new GameDialoguePacket(character.getDisplayName(), "Ora potremmo andar via o esplorare un altro po'..."), false
                ));
                responses.add(new CommandResponse(
                        new GameNarratorPacket("Se desideri uscire cancella la stanza o disconnettiti"), false)
                );
            }
        } else {
            responses.add(new CommandResponse(new ErrorPacket(ErrorCode.NO_ROOM), false));
        }

        return responses;
    }

    /**
     * Risponde con un messaggio di stanza bloccata, eventualmente personalizzato
     * a seconda della destinazione.
     *
     * @param character Personaggio che ha tentato lo spostamento
     * @param nextRoom  Stanza verso cui si stava cercando di andare
     * @param gameState Stato di gioco corrente
     * @return lista di risposte {@link CommandResponse}
     */
    private List<CommandResponse> lockedRoom(Character character, Room nextRoom, GameState gameState) {
        List<CommandResponse> responses = new ArrayList<>();

        switch (nextRoom.getName()) {
            case "elevator" -> {
                responses.add(new CommandResponse(
                        new ErrorPacket(ErrorCode.LOCKED_ROOM, "Non puoi attraversare le porte dell'ascensore... Devi prima chiamarlo"),
                        false
                ));
            }
            case "ballroom" -> {
                responses.add(new CommandResponse(
                        new ErrorPacket(ErrorCode.LOCKED_ROOM, "Non hai sofferto abbastanza, non puoi ballare."),
                        false
                ));
            }
            case "hallway_12_s", "hallway_12_ne", "hallway_12_nw" -> {
                responses.add(new CommandResponse(
                        new ErrorPacket(ErrorCode.LOCKED_ROOM, "USA LO ZAINO PROTONICO!"),
                        false
                ));
            }
            case "room_1202" -> {
                responses.add(new CommandResponse(
                        new ErrorPacket(ErrorCode.LOCKED_ROOM, """
                                        La porta è chiusa, devi trovare un modo per aprirla.
                                        Potresti cercare il proprietario della stanza, magari è giù che aspetta l'ascensore.\
                                        """),
                        false
                ));
            }
            case "room_1203" -> {
                responses.add(new CommandResponse(
                        new ErrorPacket(ErrorCode.LOCKED_ROOM, """
                                        La porta è chiusa, devi trovare un modo per aprirla.\
                                        """),
                        false
                ));
                if (gameState.hasFlag(GameFlag.UNLOCK_ROOM_1202)) {
                    responses.add(new CommandResponse(
                            new GameNarratorPacket("""
                                    È già la seconda stanza chiusa, magari ti conviene trovare la chiave universale.
                                    Potrebbe averla la signora delle pulizie di prima...\
                                    """),
                            true
                    ));
                }
            }
            case "room_1205" -> {
                responses.add(new CommandResponse(
                        new ErrorPacket(ErrorCode.LOCKED_ROOM, """
                                        La porta è chiusa, ma senti dei rumori provenire dall'interno.
                                        Potrebbe essere la signora delle pulizie di prima. Prova a parlarci.\
                                        """),
                        false
                ));
            }
            default -> {
                responses.add(new CommandResponse(new ErrorPacket(ErrorCode.LOCKED_ROOM), false));
            }
        }

        return responses;
    }

    /**
     * Gestisce l'ingresso in una stanza che prevede una scena speciale se vi si accede
     * per la prima volta (flag non ancora consumato).
     *
     * @param character Personaggio che si muove
     * @param nextRoom  Stanza di destinazione
     * @param gameDescription  Descrizione statica del gioco
     * @param gameState Stato di gioco corrente
     * @return lista di risposte {@link CommandResponse}
     */
    private List<CommandResponse> firstTime(Character character, Room nextRoom, GameDescription gameDescription, GameState gameState) {
        List<CommandResponse> responses = new ArrayList<>();

        switch (nextRoom.getName()) {
            case "hall_elevator" -> {
                if (!gameState.removeFlag(GameFlag.FIRST_TIME_HALL_ELEVATOR)) {
                    break;
                }

                responses.add(new CommandResponse(
                        new GameNarratorPacket("Un inutile vecchietto impiccione si avvicina"),
                        true
                ));
                responses.add(new CommandResponse(
                        new GameDialoguePacket("Ospite", "Voi chi dovreste essere? Una specie di cosmonauti?"),
                        true
                ));
            }
            case "elevator" -> {
                if (!gameState.removeFlag(GameFlag.FIRST_TIME_ELEVATOR)) {
                    break;
                }

                responses.add(new CommandResponse(
                        new GameDialoguePacket(gameState.getCharacter("ray").getDisplayName(),
                                "Stavo pensando. Ma questo equipaggiamento non ha mai avuto un collaudo."
                        ),
                        true
                ));
                responses.add(new CommandResponse(
                        new GameDialoguePacket(gameState.getCharacter("egon").getDisplayName(),
                                "Me lo sto rimproverando."
                        ),
                        true
                ));
                responses.add(new CommandResponse(
                        new GameDialoguePacket(gameState.getCharacter("ray").getDisplayName(),
                                "Non ha senso preoccuparsi ora."
                        ),
                        true
                ));
                responses.add(new CommandResponse(
                        new GameDialoguePacket(gameState.getCharacter("peter").getDisplayName(),
                                "Perché preoccuparsi? Ognuno di noi porta sulla schiena un acceleratore nucleare non autorizzato."
                        ),
                        true
                ));
                responses.add(new CommandResponse(
                        new GameDialoguePacket(gameState.getCharacter("ray").getDisplayName(),
                                "Sì. Prepariamoci."
                        ),
                        true
                ));
            }
            case "hallway_12_elevator" -> {
                if (!gameState.hasFlag(GameFlag.TUTORIAL_PROTON_BACKPACK)) {
                    break;
                } // questo flag viene rimosso dopo l'utilizzo del proton_backpack

                responses.add(new CommandResponse(
                        new GameNarratorPacket("Senti un rumore da dietro l'angolo.\n" +
                                "PRESTO! USA IL TUO ZAINO PROTONICO!"),
                        true
                ));
            }
            case "hallway_12_se" -> {
                if (!gameState.removeFlag(GameFlag.FIRST_TIME_HALLWAY_12_SE)) {
                    break;
                }

                responses.add(new CommandResponse(
                        new GameNarratorPacket("""
                                Appare davanti a te un piccolo fantasma verde che sembra una schifosissima caccola: è SLIMER.\
                                """),
                        true
                ));
                switch (character.getName()) {
                    case "peter" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Ma guarda… un gigantesco *moccio* verde volante!\
                                    """),
                            true
                    ));
                    case "ray" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Classe 5: manifestazione corporea completa… ed è stupendo!\
                                    """),
                            true
                    ));
                    case "egon" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Eccolo. Notare la densità delle spore e la luminescenza del protoplasma… impressionante.\
                                    """
                            ),
                            true
                    ));
                }
                responses.add(new CommandResponse(
                        new GameNarratorPacket("""
                                Il fantasma ti ha notato...\
                                """
                        ),
                        true
                ));
                responses.add(new CommandResponse(
                        new GameDialoguePacket(gameState.getNPC("slimer").getDisplayName(), """
                                    ...AaAaAaAaAaAaAa...\
                                    """
                        ),
                        true
                ));
                responses.add(new CommandResponse(
                        new GameNarratorPacket("""
                                Il fantasma è fuggito attraverso la parete.
                                Prova ad usare il tuo Rilevatore P.K.E. per individuare dove è andato.\
                                """),
                        true
                ));

                gameState.getNPC("slimer").move(gameDescription.getGameMap().getRoom("room_1202"));

            }
            case "room_1202" -> {
                if (!gameState.removeFlag(GameFlag.FIRST_TIME_ROOM_1202)) {
                    break;
                }

                gameState.getNPC("slimer").move(gameDescription.getGameMap().getRoom("room_1203"));

                responses.add(new CommandResponse(
                        new GameNarratorPacket("""
                                Finalmente sei riuscito ad entrare, ma il fantasma non è più qui.\
                                """),
                        true
                ));

                if (gameState.hasFlag(GameFlag.GUEST_1202)) {
                    responses.add(new CommandResponse(
                            new GameDialoguePacket("Ospite 1202", """
                                    Ma qui è tutto distrutto, tutte le mie preziose cose!!!
                                    Il direttore ne sarà informato...\
                                    """),
                            true
                    ));
                    responses.add(new CommandResponse(
                            new GameDialoguePacket(gameState.getCharacter("peter").getDisplayName(), """
                                    Capisco… ma guardi il lato positivo: lei è ancora vivo, lui no.\
                                    """),
                            true
                    ));
                }

                responses.add(new CommandResponse(
                        new GameNarratorPacket("""
                                Poltrone capovolte, tovaglie lacerate e schizzi di ectoplasma ovunque: sembra che un buffet abbia perso la battaglia.\
                                """),
                        true
                ));

                switch (character.getName()) {
                    case "peter" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Beh… direi che questo posto ha decisamente bisogno di un interior designer. Magari morto, così si sente a casa.\
                                    """),
                            true
                    ));
                    case "ray" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Accidenti… ha lasciato una bella scia ectoplasmatica! Quasi una firma. È un segno che è ancora vicino.\
                                    """),
                            true
                    ));
                    case "egon" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Notevole dispersione di ectoplasma. La quantità raccolta qui basterà per settimane di analisi.\
                                    """),
                            true
                    ));
                }
            }
            case "room_1203" -> {
                if (!gameState.removeFlag(GameFlag.FIRST_TIME_ROOM_1203)) {
                    break;
                }

                responses.add(new CommandResponse(
                        new GameNarratorPacket("""
                                Finalmente sei riuscito ad entrare, ecco SLIMER!\
                                """),
                        true
                ));

                switch (character.getName()) {
                    case "peter" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Oh guarda, il nostro caro amico moccioso è tornato! Vieni qui, verde brillante, ti prometto che non farà male… troppo.\
                                    """),
                            true
                    ));
                    case "ray" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Lo sapevo che era qui! Sta’ fermo, vecchio amico, non vogliamo farti del male… se collabori.\
                                    """),
                            true
                    ));
                    case "egon" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Confermo: manifestazione ectoplasmatica piena, classe 5. Prepararsi all’ingaggio.\
                                    """),
                            true
                    ));
                }

                responses.add(new CommandResponse(
                        new GameDialoguePacket(gameState.getNPC("slimer").getDisplayName(), """
                                    ...AHAHAHAHAHAHAHHA...\
                                    """),
                        true
                ));
                responses.add(new CommandResponse(
                        new GameNarratorPacket("""
                                SLIMER ti ha attraversato ed è fuggito!\
                                """),
                        true
                ));

                character.setDisplayName(character.getDisplayName() + " Melmoso");

                switch (character.getName()) {
                    case "peter" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Bleah! Mi ha smerdato!\
                                    """),
                            true
                    ));
                    case "ray" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Wow! Mi ha attraversato completamente! Sono ricoperto di ectoplasma… è bellissimo!\
                                    """),
                            true
                    ));
                    case "egon" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Interessante. Il residuo ectoplasmatico è denso e appiccicoso.\
                                    """),
                            true
                    ));
                }

                gameState.getNPC("slimer").move(gameDescription.getGameMap().getRoom("ballroom"));
                gameState.addFlag(GameFlag.GHOST_BALLROOM);
            }
            case "hallway_12_s" -> {
                if (!gameState.hasFlag(GameFlag.GHOST_BALLROOM)){
                    break;
                }

                responses.add(new CommandResponse(
                        new GameNarratorPacket("""
                                Arriva il direttore spaventato.\
                                """),
                        true
                ));
                responses.add(new CommandResponse(
                        new GameDialoguePacket("Direttore spaventato", """
                                    Qualcosa è entrato nella sala da ballo! Correte presto!\
                                    """),
                        true
                ));
            }
            case "ballroom" -> {
                if (!gameState.removeFlag(GameFlag.FIRST_TIME_BALLROOM)) {
                    break;
                }

                switch (character.getName()) {
                    case "peter" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Ah beh, certo… perché scendere in pista quando puoi infestare un lampadario? Genio.\
                                    """),
                            true
                    ));
                    case "ray" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Guardalo! Sta roteando come un bambino a una festa! È davvero in forma!\
                                    """),
                            true
                    ));
                    case "egon" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Notevole. L’energia cinetica che sprigiona potrebbe mandare in frantumi ogni cristallo del lampadario.\
                                    """),
                            true
                    ));
                }

                responses.add(new CommandResponse(
                        new GameDialoguePacket(gameState.getCharacter("egon").getDisplayName(), """
                                    Ricordate: non incrociate i flussi. Potrebbe… beh, diciamo che non finirebbe bene.
                                    Prendiamo la trappola, va posizionata al centro. Al mio segnale, va usata.\
                                    """),
                        true
                ));

                Item trap = new Item("ghost_trap", "Trappola per Fantasmi",
                        "Un dispositivo portatile progettato per contenere entità ectoplasmatiche. "
                );

                gameState.getCharacters().forEach(c -> c.addItem(trap));

                responses.add(new CommandResponse(
                        new GameNarratorPacket("Hai ricevuto " + trap.getDisplayName() + " usala per posizionarla al centro"),
                        true
                ));
            }
        }

        return responses;
    }

    /**
     * Esegue il comando "vai" spostando il personaggio nella direzione indicata,
     * se valida e disponibile. Gestisce risposte di errore, casi speciali, scene
     * di “prima volta” e movimento multiplo in ascensore.
     *
     * @param character        Personaggio che ha invocato il comando
     * @param gameDescription  Descrizione statica del gioco
     * @param gameState        Stato dinamico del gioco
     * @param args             Argomenti del comando (input utente)
     * @return lista di {@link CommandResponse} da inviare al client
     */
    @Override
    public List<CommandResponse> execute(Character character, GameDescription gameDescription, GameState gameState, String[] args) {
        // Estrae la prima direzione valida dagli argomenti (con alias)
        Optional<String> optionalDir = Arrays.stream(args)
                .filter(arg -> directionAliases.containsKey(arg.toLowerCase()))
                .map(arg -> directionAliases.get(arg.toLowerCase()))
                .findFirst();

        if (optionalDir.isEmpty()) {
            return List.of(new CommandResponse(
                    new ErrorPacket(ErrorCode.COMMAND_WITHOUT_ARGS, "Dove vuoi andare?"),
                    false
            ));
        }

        String dir = optionalDir.get();
        Room currentRoom = character.getCurrentRoom();
        if (currentRoom == null) {
            return List.of(new CommandResponse(
                    new ErrorPacket(ErrorCode.COMMAND_NOT_AVAILABLE, "Non sei in una stanza."),
                    false
            ));
        }

        Room nextRoom = currentRoom.getConnectedRoom(dir);
        if (nextRoom == null) {
            return nullRoom(character, currentRoom, dir, gameState);
        }
        if (!currentRoom.checkConnectionFlags(dir, gameState.getFlags())) {
            return lockedRoom(character, nextRoom, gameState);
        }

        // Movimento di gruppo in ascensore o sala da ballo oppure singolo personaggio
        if (currentRoom.getName().equals("elevator") || nextRoom.getName().equals("ballroom")) {
            gameState.getCharacters().forEach(c -> c.move(nextRoom));
            gameState.removeFlag(GameFlag.ELEVATOR_OPEN);
        } else {
            character.move(nextRoom);
        }

        ArrayList<CommandResponse> responses = new ArrayList<>();
        // Annuncio della nuova stanza, broadcast se si è usciti dall’ascensore
        responses.add(new CommandResponse(
                new GameNarratorPacket(nextRoom.getDescription()),
                currentRoom.getName().equals("elevator")
        ));

        responses.addAll(firstTime(character, nextRoom, gameDescription, gameState));

        /*
         * Eventuale blocco per promemoria protonico, non attivo in questa versione.
         */

        return responses;
    }
}
