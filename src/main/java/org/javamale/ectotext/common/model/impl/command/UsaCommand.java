package org.javamale.ectotext.common.model.impl.command;

import org.javamale.ectotext.common.model.*;
import org.javamale.ectotext.common.model.Character;
import org.javamale.ectotext.common.packet.ErrorCode;
import org.javamale.ectotext.common.packet.impl.ErrorPacket;
import org.javamale.ectotext.common.packet.impl.GameDialoguePacket;
import org.javamale.ectotext.common.packet.impl.GameNarratorPacket;

import java.util.*;

/**
 * Gestisce il comando "usa" per l'utilizzo degli oggetti nell'inventario.
 * <p>
 * Permette ai giocatori di:
 * <ul>
 *   <li>Utilizzare oggetti specifici come zaino protonico e P.K.E.</li>
 *   <li>Interagire con l'ambiente di gioco</li>
 *   <li>Sbloccare nuovi contenuti tramite l'uso di oggetti chiave</li>
 * </ul>
 * La logica di utilizzo varia in base all'oggetto e al contesto.
 */
public class UsaCommand implements CommandHandler {

    /**
     * Gestisce l'utilizzo dello zaino protonico.
     * <p>
     * Le azioni possibili includono:
     * <ul>
     *   <li>Tutorial iniziale con il carrello delle pulizie</li>
     *   <li>Tentativo di cattura del fantasma</li>
     *   <li>Interazioni speciali in determinate stanze</li>
     * </ul>
     *
     * @param character il personaggio che usa lo zaino
     * @param currentRoom la stanza corrente
     * @param gameState lo stato del gioco
     * @return lista di risposte in base all'utilizzo
     */
    private List<CommandResponse> useProtonBackpack(Character character, Room currentRoom, GameState gameState) {
        List<CommandResponse> responses = new ArrayList<>();

        switch (currentRoom.getName()){
            case "hallway_12_elevator" -> {
                if (gameState.removeFlag(GameFlag.TUTORIAL_PROTON_BACKPACK)){
                    gameState.addFlag(GameFlag.TUTORIAL_PROTON_BACKPACK_COMPLETE);

                    responses.add(new CommandResponse(
                            new GameNarratorPacket("Era la signora delle pulizie, hai appena polverizzato il suo carrello..."),
                            true)
                    );
                    responses.add(new CommandResponse(
                            new GameDialoguePacket("Signora delle pulizie", "Ma cosa vi ho fatto?"),
                            true
                    ));
                    switch (character.getName()){
                        case "peter" -> responses.add(new CommandResponse(
                                new GameDialoguePacket(character.getDisplayName(),
                                        "Ci scusi, l'abbiamo presa per un'altra."
                                ),
                                true
                        ));
                        case "ray" -> responses.add(new CommandResponse(
                                new GameDialoguePacket(character.getDisplayName(), "Ci scusi."),
                                true
                        ));
                        case "egon" -> responses.add(new CommandResponse(
                                new GameDialoguePacket(character.getDisplayName(), "Scusi."),
                                true
                        ));
                    }
                    responses.add(new CommandResponse(
                            new GameNarratorPacket("La signora delle pulizie fugge per la sua vita."),
                            true)
                    );
                    responses.add(new CommandResponse(
                            new GameDialoguePacket(gameState.getCharacter("peter").getDisplayName(),
                                    "Collaudo positivo"
                            ),
                            true
                    ));
                } else {
                    responses.add(new CommandResponse(
                            new GameDialoguePacket(gameState.getCharacter("peter").getDisplayName(),
                                    "Collaudo già positivo, non c'è più nulla a cui sparare cowboy"
                            ),
                            false
                    ));
                }
            }
            case "room_1202", "room_1203" -> {
                responses.add(new CommandResponse(
                        new GameDialoguePacket(gameState.getCharacter("peter").getDisplayName(), """
                                Forse è meglio controllare col rilevatore prima di incenerire tutto.
                                Ti ricordo che devi cercare di colpire qualcosa che si muove.\
                                """
                        ),
                        false
                ));
            }
            case "ballroom" -> {
                if (gameState.hasFlag(GameFlag.GHOST_CAPTURED)){
                    responses.add(new CommandResponse(
                            new GameDialoguePacket(gameState.getCharacter("peter").getDisplayName(), """
                                Il fantasma è già stato catturato.
                                Rilassa il dito, cowboy, e lascia stare il grilletto… almeno finché non appare qualcos’altro di verdognolo.\
                                """
                            ),
                            false
                    ));
                    break;
                }

                if (!gameState.hasFlag(GameFlag.GHOST_TRAP_PLACED)){
                    responses.add(new CommandResponse(
                            new GameNarratorPacket("Provi a colpire il fantasma ma schiva il colpo."),
                            false)
                    );
                    break;
                }

                responses.add(new CommandResponse(
                        new GameNarratorPacket("Colpisci il fantasma, ora è immobile sulla trappola!"),
                        true)
                );

                gameState.addFlag(GameFlag.GHOST_TRAP_READY);

                responses.add(new CommandResponse(
                        new GameDialoguePacket(gameState.getCharacter("egon").getDisplayName(), """
                                Attivate la trappola! Ora!\
                                """
                        ),
                        true
                ));
            }
            default -> {
                responses.add(new CommandResponse(
                        new GameNarratorPacket("Non hai nulla a cui sparare qui."),
                        false)
                );
            }
        }

        return responses;
    }

    /**
     * Gestisce l'utilizzo del rilevatore P.K.E.
     * <p>
     * Il dispositivo:
     * <ul>
     *   <li>Rileva la presenza di fantasmi nelle vicinanze</li>
     *   <li>Indica la direzione della fonte paranormale</li>
     *   <li>Non funziona se il fantasma è già stato catturato</li>
     * </ul>
     *
     * @param character il personaggio che usa il rilevatore
     * @param currentRoom la stanza corrente
     * @param gameState lo stato del gioco
     * @return lista di risposte con l'indicazione della direzione
     */
    private List<CommandResponse> usePkeMeter(Character character, Room currentRoom, GameState gameState){
        List<CommandResponse> responses = new ArrayList<>();

        String direction = null;

        switch (gameState.getNPC("slimer").getCurrentRoom().getName()){
            case "hallway_12_se" -> direction = switch (currentRoom.getName()) {
                case "hallway_12_sw", "hallway_12_s" -> "NORD";
                case "hallway_12_ne" -> "SUD";
                case "room_1202", "room_1204", "room_1206" -> "OVEST";
                case "hallway_12_elevator", "hallway_12_nw", "room_1201", "room_1203", "room_1205" -> "EST";
                case "hallway_12_se" -> "QUI";
                default -> null;
            };
            case "room_1202" -> direction = switch (currentRoom.getName()) {
                case "hallway_12_se", "hallway_12_s" -> "NORD";
                case "hallway_12_nw" -> "SUD";
                case "hallway_12_elevator", "hallway_12_ne", "room_1204", "room_1206" -> "OVEST";
                case "hallway_12_sw", "room_1201", "room_1203", "room_1205" -> "EST";
                case "room_1202" -> "QUI";
                default -> null;
            };
            case "room_1203" -> direction = switch (currentRoom.getName()) {
                case "hallway_12_sw", "hallway_12_se" -> "NORD";
                case "hallway_12_elevator" -> "SUD";
                case "hallway_12_ne", "hallway_12_s", "room_1202", "room_1204", "room_1206" -> "OVEST";
                case "hallway_12_nw", "room_1201", "room_1205" -> "EST";
                case "room_1203" -> "QUI";
                default -> null;
            };
            case "ballroom" -> direction = switch (currentRoom.getName()) {
                case "hall_elevator" -> "SUD";
                case "hall" -> "EST";
                case "ballroom" -> "QUI";
                default -> null;
            };
        }

        if (gameState.hasFlag(GameFlag.GHOST_CAPTURED)) {
            direction = null;
        }

        if (direction == null){
            responses.add(new CommandResponse(
                    new GameNarratorPacket("Non rilevi nulla."),
                    false)
            );
        } else {
            responses.add(new CommandResponse(
                    new GameNarratorPacket("Rilevi dei segnali da " + direction + "."),
                    false)
            );
        }

        return responses;
    }

    /**
     * Gestisce l'utilizzo del mocio per la pulizia.
     * <p>
     * Permette di:
     * <ul>
     *   <li>Pulire la melma in determinate aree</li>
     *   <li>Sbloccare oggetti nascosti</li>
     *   <li>Generare dialoghi contestuali</li>
     * </ul>
     *
     * @param character il personaggio che usa il mocio
     * @param currentRoom la stanza corrente
     * @param gameState lo stato del gioco
     * @return lista di risposte narrative all'azione
     */
    private List<CommandResponse> useMocio(Character character, Room currentRoom, GameState gameState) {
        List<CommandResponse> responses = new ArrayList<>();

        switch (currentRoom.getName()){
            case "hallway_12_nw" -> {
                if (gameState.hasFlag(GameFlag.HALLWAY_12_NW_CLEAN)) {
                    responses.add(new CommandResponse(
                            new GameNarratorPacket("""
                                Troppo tardi, campione: qui è già tutto pulito.
                                Non è il caso di insistere, a meno che tu non voglia lucidare la moquette.\
                                """),
                            false)
                    );
                    break;
                }

                responses.add(new CommandResponse(
                        new GameNarratorPacket("""
                                La chiave è completamente ricoperta di melma verdognola.
                                Forse è il caso di pulirla prima di prenderla… a meno che tu non abbia sempre sognato dita fluorescenti.\
                                """),
                        false)
                );

                switch (character.getName()){
                    case "peter" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Ecco, il duro lavoro è fatto. Ora qualcuno mi passi una medaglia… o almeno del sapone!\
                                    """
                            ),
                            true
                    ));
                    case "ray" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Credo di aver sviluppato una nuova tecnica di pulizia: Ray-wash!\
                                    """),
                            true
                    ));
                    case "egon" -> responses.add(new CommandResponse(
                            new GameDialoguePacket(character.getDisplayName(), """
                                    Melma rimossa. Efficienza sopra la media. Prendo nota.\
                                    """),
                            true
                    ));
                }

                responses.add(new CommandResponse(
                        new GameNarratorPacket("""
                                La chiave è lì, pulita e (quasi) innocua. Tocca a te prenderla… e scoprire se oggi era davvero il tuo giorno fortunato.\
                                """),
                        false)
                );

                gameState.addFlag(GameFlag.HALLWAY_12_NW_CLEAN);

            }
            case "room_1202" -> responses.add(new CommandResponse(
                    new GameNarratorPacket("La melma ti guarda, il mocio ti guarda… nessuno dei due sembra fidarsi di te."),
                    false)
            );
            case "room_1203" -> responses.add(new CommandResponse(
                    new GameDialoguePacket(gameState.getCharacter("egon").getDisplayName(),
                            "Statisticamente, il momento migliore per pulire era cinque minuti fa. Ora è solo spreco di energia."),
                    false)
            );
            case "hallway_12_se" -> responses.add(new CommandResponse(
                    new GameNarratorPacket("Con questo talento, rischi solo di lucidare la melma. Meglio lasciar fare agli esperti."),
                    false)
            );
            default -> responses.add(new CommandResponse(
                    new GameNarratorPacket("Ci hai provato… ma il mocio sembra più confuso di te. Forse è meglio andare a caccia di fantasmi."),
                    false)
            );
        }

        return responses;
    }

    /**
     * Gestisce l'utilizzo della trappola per fantasmi.
     * <p>
     * Il processo include:
     * <ul>
     *   <li>Posizionamento della trappola</li>
     *   <li>Cattura del fantasma se immobilizzato</li>
     *   <li>Gestione della scena finale</li>
     * </ul>
     *
     * @param character il personaggio che usa la trappola
     * @param currentRoom la stanza corrente
     * @param gameState lo stato del gioco
     * @return lista di risposte che descrivono l'esito
     */
    private List<CommandResponse> useGhostTrap(Character character, Room currentRoom, GameState gameState) {
        if (!currentRoom.getName().equals("ballroom")) {
            return List.of(new CommandResponse(
                    new GameNarratorPacket("Non avrete mica pensato di catturare la polvere, vero? Per quella vi serve l’aspirapolvere, non la trappola!"),
                    false)
            );
        }

        if (gameState.hasFlag(GameFlag.GHOST_CAPTURED)){
            return List.of(new CommandResponse(
                    new GameNarratorPacket("""
                            La trappola scatta, si illumina… e resta vuota.
                            Il fantasma è già bello che in gabbia.
                            Puoi solo catturare la polvere, ma non farà notizia.\
                            """),
                    false)
            );
        }

        if (!gameState.hasFlag(GameFlag.GHOST_TRAP_PLACED)){
            gameState.addFlag(GameFlag.GHOST_TRAP_PLACED);
            return List.of(new CommandResponse(
                    new GameNarratorPacket("""
                            La trappola è pronta.
                            Ora manca solo un fantasma che abbia il coraggio di metterci piede sopra…
                            O di essere trascinato, come da manuale.\
                            """),
                    false)
            );
        }

        if (!gameState.hasFlag(GameFlag.GHOST_TRAP_READY)){
            return List.of(new CommandResponse(
                    new GameNarratorPacket("""
                            Premi il pulsante. La trappola si apre con un lampo…
                            Il fantasma ti guarda, fa spallucce (per quanto possibile) e fugge via come se niente fosse.\
                            """),
                    false)
            );
        }

        List<CommandResponse> responses = new ArrayList<>();

        gameState.addFlag(GameFlag.GHOST_CAPTURED);

        responses.add(new CommandResponse(
                new GameNarratorPacket("""
                        La trappola si chiude con un CLACK sfrigolante, le luci lampeggiano e un ultimo ululato ectoplasmatico risuona nell’aria…
                        Complimenti: il fantasma è in trappola!
                        La stanza, per la prima volta, sa solo di sudore e di vittoria.\
                        """
                ),
                true)
        );

        responses.add(new CommandResponse(
                new GameDialoguePacket(gameState.getNPC("slimer").getDisplayName(), """
                      AAAAAAAAaaaaaaaaa......\
                      """
                ),
                true
        ));

        responses.add(new CommandResponse(
                new GameDialoguePacket(gameState.getCharacter("peter").getDisplayName(), """
                       Venimmo, vedemmo e lo inculammo!\
                       """
                ),
                true
        ));

        responses.add(new CommandResponse(
                new GameDialoguePacket(gameState.getCharacter("ray").getDisplayName(), """
                        Wow, che corsa ragazzi! Direi che il paziente è in condizione… ehm, stazionaria.\
                        """
                ),
                true
        ));

        responses.add(new CommandResponse(
                new GameDialoguePacket(gameState.getCharacter("egon").getDisplayName(), """
                        Operazione conclusa. Dati raccolti: abbondanti. Entropia ambientale rientrata al 12 %.
                        Resta solo da analizzare la melma residua. Bel lavoro.\
                        """
                ),
                true
        ));

        responses.add(new CommandResponse(
                new GameDialoguePacket("Direttore", """
                        Che cosa avete visto? Lo avete preso?! M-ma... guardate cosa avete combinato!
                        Sedie volate ovunque, il buffet distrutto... il salone è un disastro!\
                        """
                ),
                true
        ));

        responses.add(new CommandResponse(
                new GameDialoguePacket(gameState.getCharacter("ray").getDisplayName(), """
                        Lo abbiamo preso! Ciò che avevate era una vapore a erranza di 5 classe, uno di quelli cattivi...\
                        """
                ),
                true
        ));

        responses.add(new CommandResponse(
                new GameDialoguePacket(gameState.getCharacter("peter").getDisplayName(), """
                       E ora... parliamo di cose serie. Per l'intrappolamento le dovremo chiedere 4000$...
                       Ma è settimana di sconti speciali per il refill protonico e lo stoccaggio della bastia
                       Quindi quello vi verrà solo 1000$ per fortuna vostra\
                       """
                ),
                true
        ));

        responses.add(new CommandResponse(
                new GameDialoguePacket("Direttore", """
                        5000$?! È un'esagerazione io non vi pago!\
                        """
                ),
                true
        ));

        responses.add(new CommandResponse(
                new GameDialoguePacket(gameState.getCharacter("peter").getDisplayName(), """
                       Va bene! Possiamo sempre rimetterlo al suo posto! Prego dott. Ray\
                       """
                ),
                true
        ));

        responses.add(new CommandResponse(
                new GameNarratorPacket("""
                        Ray inizia ad aprire la trappola...\
                        """),
                true)
        );

        responses.add(new CommandResponse(
                new GameDialoguePacket("Direttore", """
                        No, no, no, NO! E va bene! Li avrete!\
                        """
                ),
                true
        ));

        responses.add(new CommandResponse(
                new GameDialoguePacket(gameState.getCharacter("peter").getDisplayName(), """
                       Grazie infinite! Ci vediamo eh...\
                       """
                ),
                true
        ));

        responses.add(new CommandResponse(
                new GameDialoguePacket(gameState.getCharacter("peter").getDisplayName(), """
                       Grazie, sempre a disposizione! LARGO! Qui c'è un vapore a erranza di 5 classe!\
                       """
                ),
                true
        ));

        responses.add(new CommandResponse(
                new GameNarratorPacket("""
                        E anche questa volta abbiamo salvato il mondo… o almeno la moquette dell’hotel.
                        Se c'è qualcosa di strano, nel tuo quartiere, chi chiamerai?\
                        """),
                true)
        );

        responses.add(new CommandResponse(
                new GameDialoguePacket(character.getDisplayName(), """
                                    GHOSTBUSTERS!\
                                    """
                ),
                true
        ));

        return responses;
    }

    /**
     * Gestisce l'utilizzo della chiave della stanza 1205.
     * <p>
     * Sblocca l'accesso alla stanza aggiungendo il flag appropriato.
     *
     * @param character il personaggio che usa la chiave
     * @param currentRoom la stanza corrente
     * @param gameState lo stato del gioco
     * @return lista di risposte con la conferma dello sblocco
     */
    private List<CommandResponse> useKey1205(Character character, Room currentRoom, GameState gameState) {
        gameState.addFlag(GameFlag.UNLOCK_ROOM_1205);

        return List.of(new CommandResponse(
                new GameNarratorPacket("Ora puoi entrare nella 1205."),
                false)
        );
    }

    /**
     * Esegue il comando "usa" su un oggetto dell'inventario.
     * <p>
     * Il processo prevede:
     * <ol>
     *   <li>Verifica della presenza dell'oggetto nell'inventario</li>
     *   <li>Identificazione del tipo di oggetto</li>
     *   <li>Esecuzione della logica specifica dell'oggetto</li>
     *   <li>Gestione degli errori per oggetti non utilizzabili</li>
     * </ol>
     *
     * @param character il personaggio che esegue il comando
     * @param gameDescription la descrizione statica del gioco
     * @param gameState lo stato corrente del gioco
     * @param args gli argomenti forniti (nome dell'oggetto da usare)
     * @return lista di risposte al comando
     * @throws IllegalStateException se il personaggio non è in una stanza valida
     */
    @Override
    public List<CommandResponse> execute(Character character, GameDescription gameDescription, GameState gameState, String[] args) {
        Optional<Item> optionalItem = Arrays.stream(args)
                .map(arg -> character.getInventory().stream()
                        .filter(item -> item.getDisplayName().toLowerCase().contains(arg.toLowerCase()))
                        .findFirst()
                )
                .flatMap(Optional::stream)
                .findFirst();

        if (optionalItem.isEmpty()) {
            return List.of(new CommandResponse(
                            new ErrorPacket(ErrorCode.COMMAND_WITHOUT_ARGS, "Puoi usare solo gli oggetti presenti nel tuo inventario"),
                            false
                    )
            );
        }

        Item item = optionalItem.get();
        Room currentRoom = character.getCurrentRoom();

        List<CommandResponse> responses = new ArrayList<>();

        switch (item.getName()){
            case "proton_backpack" -> responses.addAll(useProtonBackpack(character, currentRoom, gameState));
            case "pke_meter" -> responses.addAll(usePkeMeter(character, currentRoom, gameState));
            case "mocio" -> responses.addAll(useMocio(character, currentRoom, gameState));
            case "key_1205" -> responses.addAll(useKey1205(character, currentRoom, gameState));
            case "ghost_trap" -> responses.addAll(useGhostTrap(character, currentRoom, gameState));
        }

        return responses;
    }
}