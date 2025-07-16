package org.javamale.ectotext.common.model.impl.command;

import org.javamale.ectotext.common.model.*;
import org.javamale.ectotext.common.model.Character;
import org.javamale.ectotext.common.packet.ErrorCode;
import org.javamale.ectotext.common.packet.impl.ErrorPacket;
import org.javamale.ectotext.common.packet.impl.GameDialoguePacket;
import org.javamale.ectotext.common.packet.impl.GameNarratorPacket;

import java.util.*;

/**
 * Classe ParlaCommand.
 * <p>
 * Responsabilità principale di ParlaCommand: gestisce il comando per interagire e parlare con NPC
 * o altri personaggi all'interno della stanza, fornendo risposte contestuali a seconda del target.
 * </p>
 */
public class ParlaCommand implements CommandHandler {
    private static final Map<String, String> targetAliases = Map.ofEntries(
            Map.entry("manager", "manager"), Map.entry("direttore", "manager"),
            Map.entry("ospite", "guest"), Map.entry("vecchio", "guest"), Map.entry("vecchietto", "guest"),
            Map.entry("signora", "cleaning_lady"), Map.entry("pulizie", "cleaning_lady")
    );

    /**
     * Genera la risposta al dialogo con il direttore (manager).
     *
     * @param character   il personaggio che parla
     * @param currentRoom la stanza corrente del personaggio
     * @param gameState   lo stato corrente del gioco
     * @return lista di risposte {@link CommandResponse} in seguito al dialogo con il direttore
     */
    private List<CommandResponse> managerDialogue(Character character, Room currentRoom, GameState gameState) {
        List<CommandResponse> responses = new ArrayList<>();

        switch (currentRoom.getName()) {
            case "hall" -> {
                if(gameState.hasFlag(GameFlag.GHOST_CAPTURED)){
                    responses.add(new CommandResponse(
                            new GameDialoguePacket("Direttore",
                                    "Ho già detto che va bene! Avrete i vostri soldi! Ora andate via."
                            ),
                            true
                    ));
                    break;
                }
                switch (character.getName()) {
                    case "peter" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(),
                                    "Coraggio coraggio. Spiegami la situazione!"
                            ),
                            true
                    ));
                    case "ray" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(),
                                    "Non tema, trattiamo queste cose continuamente. " +
                                            "Ci spieghi meglio la situazione!"
                            ),
                            true
                    ));
                    case "egon" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(),
                                    "Ne ha parlato con qualcuno? Mi dia i dettagli del fenomeno!"
                            ),
                            true
                    ));
                }
                responses.add(new CommandResponse(
                        new GameDialoguePacket("Direttore",
                                "Molti dei vecchi dipendenti sanno del dodicesimo piano. Dei fastidi intendo " +
                                        "dire. Però erano cessati da anni, ma ora da due settimane sono ripresi e non " +
                                        "erano mai stati così gravi. Il proprietario non vuole che ne parliamo. " +
                                        "Speravo che si potesse fare tutto in silenzio. Questa sera!"
                        ),
                        true
                ));
            }
            case "hallway_12_s" -> {
                if (!gameState.hasFlags(GameFlag.GHOST_BALLROOM) || gameState.hasFlag(GameFlag.GHOST_CAPTURED)) {
                    responses.add(new CommandResponse(
                            new ErrorPacket(ErrorCode.COMMAND_WITHOUT_ARGS, "Con chi vuoi parlare? I fantasmi non rispondono, di solito..."),
                            false
                    ));

                    break;
                }

                responses.add(new CommandResponse(
                        new GameDialoguePacket("Direttore spaventato", """
                                Qualcosa è entrato nella sala da ballo! Correte presto!
                                """
                        ),
                        true
                ));
            }
            default -> responses.add(new CommandResponse(
                    new ErrorPacket(ErrorCode.COMMAND_WITHOUT_ARGS, "Con chi vuoi parlare? I fantasmi non rispondono, di solito..."),
                    false
            ));
        }

        return responses;
    }

    /**
     * Genera la risposta al dialogo con l’ospite (guest).
     *
     * @param character   il personaggio che parla
     * @param currentRoom la stanza corrente del personaggio
     * @param gameState   lo stato corrente del gioco
     * @return lista di risposte {@link CommandResponse} in seguito al dialogo con l’ospite
     */
    private List<CommandResponse> guestDialogue(Character character, Room currentRoom, GameState gameState) {
        List<CommandResponse> responses = new ArrayList<>();

        switch (currentRoom.getName()) {
            case "hall_elevator" -> {
                if (!gameState.getNPC("slimer").getCurrentRoom().getName().equals("room_1202") &&
                        !gameState.hasFlag(GameFlag.GUEST_1202)) {
                    switch (character.getName()) {
                        case "peter" -> responses.add(new CommandResponse(
                                new GameDialoguePacket(character.getDisplayName(),
                                        "Nooo, siamo disinfestatori. Hanno visto uno scarafaggio enorme al " +
                                                "dodicesimo piano, stacca le teste a morsi."
                                ),
                                true
                        ));
                        case "ray" -> responses.add(new CommandResponse(
                                new GameDialoguePacket(character.getDisplayName(),
                                        "Noi siamo degli acchiappa fantasmi. Ma il direttore ci ha chiesto di " +
                                                "rimanere discreti quindi lo tenga per se."
                                ),
                                true
                        ));
                        case "egon" -> responses.add(new CommandResponse(
                                new GameDialoguePacket(character.getDisplayName(),
                                        "Siamo degli scienziati. Siamo qui per studiare un fenomeno paranormale."
                                ),
                                true
                        ));
                    }

                    responses.add(new CommandResponse(
                            new GameDialoguePacket(gameState.getCharacter("ray").getDisplayName(),
                                    "Sale?"
                            ),
                            true
                    ));
                    responses.add(new CommandResponse(
                            new GameDialoguePacket("Ospite",
                                    "No grazie, aspetto il prossimo."
                            ),
                            true
                    ));

                    break;
                }

                switch (character.getName()) {
                    case "peter" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Ok, lasciamo perdere gli scarafaggi… lei è il proprietario della 1202, vero?
                                    Ci sarebbe una… “cosa” molto vivace che si è chiusa lì dentro. Ce la apre?
                                    """
                            ),
                            true
                    ));
                    case "ray" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Scusi, lei per caso è il proprietario della stanza 1202?
                                    Abbiamo localizzato una forte attività paranormale al suo interno… può darci una mano?
                                    """
                            ),
                            true
                    ));
                    case "egon" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Lei risulta assegnato alla 1202?
                                    Il nostro strumento ha rilevato un’entità di Classe 5 nella sua stanza.
                                    È essenziale che collabori e ci accompagni.
                                    """
                            ),
                            true
                    ));
                }

                responses.add(new CommandResponse(
                        new GameDialoguePacket("Ospite 1202", """
                                La 1202? Sì… è la mia stanza…
                                Beh, se c’è qualcosa di pericoloso meglio che vi apra subito. Seguitemi.
                                """
                        ),
                        true
                ));

                gameState.addFlag(GameFlag.GUEST_1202);
                gameState.addFlag(GameFlag.UNLOCK_ROOM_1202);
            }
            case "room_1202" -> {
                if (!gameState.hasFlag(GameFlag.GUEST_1202)) {
                    responses.add(new CommandResponse(
                            new ErrorPacket(ErrorCode.COMMAND_WITHOUT_ARGS, "Con chi vuoi parlare? I fantasmi non rispondono, di solito..."),
                            false
                    ));
                    break;
                }

                responses.add(new CommandResponse(
                        new GameDialoguePacket("Ospite 1202", """
                                Ma qui è tutto distrutto, tutte le mie preziose cose!!!
                                Il direttore ne sarà informato...
                                """
                        ),
                        true
                ));
            }
            default -> responses.add(new CommandResponse(
                    new ErrorPacket(ErrorCode.COMMAND_WITHOUT_ARGS, "Con chi vuoi parlare? I fantasmi non rispondono, di solito..."),
                    false
            ));
        }

        return responses;
    }

    /**
     * Genera la risposta al dialogo con la signora delle pulizie (cleaning_lady).
     *
     * @param character   il personaggio che parla
     * @param currentRoom la stanza corrente del personaggio
     * @param gameState   lo stato corrente del gioco
     * @return lista di risposte {@link CommandResponse} in seguito al dialogo con la signora delle pulizie
     */
    private List<CommandResponse> cleaningLadyDialogue(Character character, Room currentRoom, GameState gameState) {
        List<CommandResponse> responses = new ArrayList<>();

        switch (currentRoom.getName()) {
            case "hallway_12_se" -> {
                if (gameState.hasFlag(GameFlag.UNLOCK_ROOM_1205)) {
                    responses.add(new CommandResponse(
                            new ErrorPacket(ErrorCode.COMMAND_WITHOUT_ARGS, "Con chi vuoi parlare? I fantasmi non rispondono, di solito..."),
                            false
                    ));

                    break;
                }

                responses.add(new CommandResponse(
                        new GameNarratorPacket("""
                                Bussi alla porta della 1205...\
                                """),
                        true
                ));

                switch (character.getName()) {
                    case "peter" -> {
                        responses.add(new CommandResponse(
                                new GameDialoguePacket(character.getDisplayName(), """
                                        Signora, sono Peter. Qui dentro ci sono io, non un fantasma!
                                        E le assicuro che sono molto meno appiccicoso. O almeno, oggi\
                                        """),
                                true
                        ));

                        responses.add(new CommandResponse(
                                new GameDialoguePacket("Signora delle pulizie", """
                                        Come faccio a sapere che non siete uno di quei cosi… spettrali?\
                                        """),
                                true
                        ));

                        responses.add(new CommandResponse(
                                new GameDialoguePacket(character.getDisplayName(), """
                                        Perché i fantasmi non bussano. O se bussano, non usano questa educazione.
                                        E soprattutto non hanno questa voce irresistibile.
                                        Mi riconosce dalla TV? Dottor Venkman, acchiappafantasmi di fiducia!\
                                        """),
                                true
                        ));

                        gameState.addFlag(GameFlag.UNLOCK_ROOM_1205);

                        responses.add(new CommandResponse(
                                new GameNarratorPacket("""
                                La signora delle pulizie sblocca la porta...\
                                """),
                                true
                        ));

                    }
                    case "ray" -> {
                        responses.add(new CommandResponse(
                                new GameDialoguePacket(character.getDisplayName(), """
                                        Mi scusi signora, ma il fantasma potrebbe essere ovunque!
                                        Lei ha mica visto della melma... verde? Appiccicosa? Radioattiva?\
                                        """),
                                true
                        ));

                        responses.add(new CommandResponse(
                                new GameDialoguePacket("Signora delle pulizie", """
                                        Certo che l’ho vista! È ovunque! Siete voi che portate sfortuna, con quegli zaini!
                                        E poi, chi siete, gli idraulici?\
                                        """),
                                true
                        ));

                        responses.add(new CommandResponse(
                                new GameDialoguePacket(character.getDisplayName(), """
                                        Noi siamo acchiappafantasmi, missione speciale!
                                        È per la scienza... e per la salvezza dell’hotel.\
                                        """),
                                true
                        ));

                    }
                    case "egon" -> {
                        responses.add(new CommandResponse(
                                new GameDialoguePacket(character.getDisplayName(), """
                                        Capisco il suo stato emotivo. Statistiche alla mano, il rischio di essere posseduti è basso.
                                        Si fidi, è per motivi di sicurezza!\
                                        """),
                                true
                        ));

                        responses.add(new CommandResponse(
                                new GameDialoguePacket("Signora delle pulizie", """
                                        Sicurezza? La sicurezza è stare dietro questa porta! Io la chiave non la do!
                                        Andate via con quei vostri cosi nucleari!\
                                        """),
                                true
                        ));

                        responses.add(new CommandResponse(
                                new GameDialoguePacket(character.getDisplayName(), """
                                        Ne prendo atto. Grazie per la collaborazione…\
                                        """),
                                true
                        ));
                    }
                }

            }
            case "room_1205" -> {
                if (gameState.hasFlag(GameFlag.UNLOCK_ROOM_1203)) {
                    responses.add(new CommandResponse(
                            new GameDialoguePacket("Signora delle pulizie", """
                                Vi ho già dato la chiave? Ora cosa volete?\
                                """),
                            true
                    ));
                    break;
                }

                switch (character.getName()) {
                    case "peter" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Avremmo bisogno della chiave universale per sistemare tutto.
                                    Così lei torna a casa prima e io mi prendo tutto il merito. Che ne dice?\
                                    """),
                            true
                    ));
                    case "ray" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Ci serve solo la chiave universale. Se ce la dà, prometto che il suo corridoio tornerà più pulito di prima. 
                                    Glielo giuro sul manuale degli acchiappafantasmi!\
                                    """),
                            true
                    ));
                    case "egon" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Abbiamo solo bisogno della chiave universale delle camere. È per un’emergenza paranormale.\
                                    """),
                            true
                    ));
                }

                responses.add(new CommandResponse(
                        new GameDialoguePacket("Signora delle pulizie", """
                                Se promettete di non raccontare che ve l’ho data... ecco qui la chiave.
                                E… fate piano, che il direttore mi fa una testa così!\
                                """),
                        true
                ));

                responses.add(new CommandResponse(
                        new GameNarratorPacket("""
                                Con la chiave master ora tutte le porte sono aperte per te.\
                                """),
                        true
                ));

                gameState.addFlags(
                        GameFlag.UNLOCK_ROOM_1201,
                        GameFlag.UNLOCK_ROOM_1202,
                        GameFlag.UNLOCK_ROOM_1203,
                        GameFlag.UNLOCK_ROOM_1204,
                        GameFlag.UNLOCK_ROOM_1205
                );
            }
            default -> responses.add(new CommandResponse(
                    new ErrorPacket(ErrorCode.COMMAND_WITHOUT_ARGS, "Con chi vuoi parlare? I fantasmi non rispondono, di solito..."),
                    false
            ));
        }

        return responses;
    }

    /**
     * Esegue il comando di dialogo.
     * <ul>
     *     <li>Identifica il target del dialogo tramite alias (direttore, ospite, pulizie, ecc).</li>
     *     <li>Chiama la funzione di dialogo corrispondente e restituisce la risposta.</li>
     *     <li>In caso di target non riconosciuto o non presente, restituisce errore contestuale.</li>
     * </ul>
     *
     * @param character       personaggio che invoca il comando
     * @param gameDescription descrizione globale del gioco
     * @param gameState       stato corrente del gioco
     * @param args            argomenti forniti dal giocatore
     * @return lista di risposte {@link CommandResponse} (errore o dialogo)
     */
    @Override
    public List<CommandResponse> execute(Character character, GameDescription gameDescription, GameState gameState, String[] args) {
        Optional<String> optionalTarget = Arrays.stream(args)
                .filter(arg -> targetAliases.containsKey(arg.toLowerCase()))
                .map(arg -> targetAliases.get(arg.toLowerCase()))
                .findFirst();
        String target = optionalTarget.orElse(null);

        if (target == null) {
            return List.of(new CommandResponse(
                            new ErrorPacket(ErrorCode.COMMAND_WITHOUT_ARGS, "Con chi vuoi parlare? I fantasmi non rispondono, di solito..."),
                            false
                    )
            );
        }

        List<CommandResponse> responses = new ArrayList<>();

        Room currentRoom = character.getCurrentRoom();

        switch (target) {
            case "manager" -> responses.addAll(managerDialogue(character, currentRoom, gameState));
            case "guest" -> responses.addAll(guestDialogue(character, currentRoom, gameState));
            case "cleaning_lady" -> responses.addAll(cleaningLadyDialogue(character, currentRoom, gameState));
        }

        return responses;
    }
}
