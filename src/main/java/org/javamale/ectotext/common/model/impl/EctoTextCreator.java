package org.javamale.ectotext.common.model.impl;

import org.javamale.ectotext.common.model.*;
import org.javamale.ectotext.common.model.Character;
import org.javamale.ectotext.common.model.impl.command.*;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.impl.GameDialoguePacket;
import org.javamale.ectotext.common.packet.impl.GameNarratorPacket;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h2>Factory di gioco “Incubo al Sedgewick Hotel”</h2>
 *
 * Implementazione di {@link GameCreator} che costruisce:
 * <ul>
 *   <li>la descrizione statica del gioco (mappa, comandi);</li>
 *   <li>lo stato dinamico iniziale (personaggi, NPC, flag);</li>
 *   <li>l’introduzione narrativa mostrata ai giocatori a inizio partita.</li>
 * </ul>
 * <p>
 * Le principali responsabilità sono suddivise nei metodi:
 * <ul>
 *   <li>{@link #createGameDescription()} – costruzione di mappa e comandi;</li>
 *   <li>{@link #createDefaultGameState(GameDescription)} – popolamento
 *       dello stato iniziale;</li>
 *   <li>{@link #gameIntro()} – generazione della sequenza di pacchetti
 *       introduttivi.</li>
 * </ul>
 */
public class EctoTextCreator implements GameCreator {

    /** Nome (leggibile) del gioco usato nei messaggi di benvenuto. */
    private final String GAME_NAME = "Incubo al Sedgewick Hotel";

    /** Mappa statica del gioco costruita da {@link #createGameMap()}. */
    private final GameMap gameMap = new GameMap();

    /** Dizionario «alias comando → {@link CommandHandler}». */
    private final Map<String, CommandHandler> gameCommands = new HashMap<>();

    /* ====================================================================== */
    /*                    IMPLEMENTAZIONE INTERFACCIA                         */
    /* ====================================================================== */

    /**
     * Costruisce la descrizione statica del gioco, popolando mappa e comandi.
     *
     * @return {@link GameDescription} pronta per essere usata dal server
     */
    @Override
    public GameDescription createGameDescription() {
        createGameMap();
        createGameCommands();
        return new GameDescription(GAME_NAME, gameMap, gameCommands);
    }

    /**
     * Genera lo {@link GameState} iniziale: personaggi giocabili, NPC e flag.
     *
     * @param gameDescription descrizione prodotta da
     *                        {@link #createGameDescription()}
     * @return stato di gioco pronto per avviare la sessione
     */
    @Override
    public GameState createDefaultGameState(GameDescription gameDescription) {
        GameState gameState = new GameState();
        GameMap gameMap = gameDescription.getGameMap();

        // Oggetti di partenza
        Item proton = new Item("proton_backpack", "Zaino Protonico",
                "Uno zaino con acceleratore nucleare");
        Item pke    = new Item("pke_meter", "Rilevatore P.K.E.",
                "Strumento per rilevare attività paranormali");

        // Personaggi giocabili
        gameState.addCharacter(
                new Character.CharacterBuilder("peter")
                        .setDisplayName("Peter Venkman")
                        .setStartingRoom(gameMap.getRoom("hall"))
                        .addItems(proton, pke)
                        .build()
        );
        gameState.addCharacter(
                new Character.CharacterBuilder("ray")
                        .setDisplayName("Ray Stants")
                        .setStartingRoom(gameMap.getRoom("hall"))
                        .addItems(proton, pke)
                        .build()
        );
        gameState.addCharacter(
                new Character.CharacterBuilder("egon")
                        .setDisplayName("Egon Spengler")
                        .setStartingRoom(gameMap.getRoom("hall"))
                        .addItems(proton, pke)
                        .build()
        );

        // NPC iniziali
        gameState.addNPC(new NPC("slimer", "Slimer",
                gameMap.getRoom("hallway_12_se")));

        // Flag iniziali
        gameState.addFlags(
                GameFlag.FIRST_TIME_HALL,
                GameFlag.FIRST_TIME_ELEVATOR,
                GameFlag.FIRST_TIME_HALL_ELEVATOR,
                GameFlag.FIRST_TIME_BALLROOM,
                GameFlag.FIRST_TIME_HALLWAY_12_SE,
                GameFlag.FIRST_TIME_ROOM_1202,
                GameFlag.FIRST_TIME_ROOM_1203,

                GameFlag.TUTORIAL_PROTON_BACKPACK
        );

        return gameState;
    }

    /**
     * Crea la sequenza di pacchetti introduttivi: benvenuto, dialoghi
     * di contesto, istruzioni iniziali.
     *
     * @return lista di {@link Packet} da inviare in ordine ai client
     */
    @Override
    public List<Packet> gameIntro() {
        return List.of(
                new GameNarratorPacket("Benvenuto in " + GAME_NAME + "\n" +
                        "Ti trovi nella hall di un elegante hotel con moquette così spessa che potresti affondarci " +
                        "una scarpa e ritrovarla nel 1972.\n" +
                        "Davanti a te, il direttore dell'hotel suda come una fontana rotta."),
                new GameDialoguePacket("Direttore",
                        "Vi prego! C’è qualcosa al dodicesimo piano... fluttua, urla e... ha mangiato il buffet da solo!"),
                new GameDialoguePacket("Ray Stants",
                        "Classe 5. Tipico. Potrebbe essere affamato. O italiano."),
                new GameNarratorPacket("""
                    Seleziona un personaggio. Puoi scegliere tra:
                     . Peter - Il leader carismatico: battuta pronta, niente paura, una mira così-così ma un'irresistibile capacità di cavarsela anche nei guai più grossi.
                     . Ray   - L'entusiasta del paranormale: cuore grande, passione per la scienza, capace di spiegare la differenza tra un ectoplasma e una manifestazione libera anche sotto stress.
                     . Egon  - Il genio silenzioso: inventore di ogni gadget, sguardo imperscrutabile dietro gli occhiali, affronta i fantasmi come un problema di matematica… e di solito ha ragione.\
                    """
                )
        );
    }

    /* ====================================================================== */
    /*                      COSTRUZIONE COMPONENTI INTERNI                    */
    /* ====================================================================== */

    /** Costruisce l’intera mappa di gioco (stanze, corridoi, collegamenti). */
    private void createGameMap() {
        // Ascensore ----------------------------------------------------------
        gameMap.addRoom(new Room.RoomBuilder("elevator")
                .setDisplayName("Ascensore")
                .setDescription("Le porte dell'ascensore si chiudono alle tue spalle con un clang metallico e un ding nervoso annuncia la prossima fermata.")
                .setLongDescription("""
                    Cabina stretta rivestita di ottone graffiato, impregnata dell’odore di moquette umida e deodorante al panico. Le lampadine tremolano a ogni scatto del motore, come se trattenessero il respiro con te. Nello specchio l’uniforme da Acchiappafantasmi – zaino protonico e spalle bruciate – stona con l’arredamento Art Déco.
                       . A PT c'è la Sala degli ascensori (piano terra)
                       . A 12 c'è il Corridoio Ascensore (12° piano)\
                    """
                )
                .build()
        );

        /* ---------------------------- 12° piano --------------------------- */
        // Stanze 1201-1206
        gameMap.addRoom(new Room.RoomBuilder("room_1201")
                .setDisplayName("Stanza 1201")
                .setDescription("La 1201 è vuota, ordinata e sorprendentemente normale.")
                .setLongDescription("""
                    La porta cigola appena mentre la apri: all'interno tutto è in perfetto ordine, dal copriletto stirato alla valigia chiusa con cura.
                    Non c'è melma, non c'è il minimo stridio di fantasmi, solo il lieve profumo di ammorbidente e un silenzio rassicurante.
                       . A EST c'è il Corridoio Sud Ovest\
                    """
                )
                .build());

        gameMap.addRoom(new Room.RoomBuilder("room_1202")
                .setDisplayName("Stanza 1202")
                .setDescription("La porta della 1202 chiazzata di melma verde appiccicosa si chiude dietro di te.")
                .setLongDescription("""
                    Schizzi di ectoplasma un po' ovunque ma sul muro est una sagoma di Slimer segna il punto in cui il fantasma ha attraversato la parete confinante con la 1203.
                       . A OVEST c'è il Corridoio Sud Ovest\
                    """
                )
                .build());

        gameMap.addRoom(new Room.RoomBuilder("room_1203")
                .setDisplayName("Stanza 1203")
                .setDescription("Varcata la soglia della 1203, un'ondata di gelo ti avvolge e il silenzio è assoluto.")
                .setLongDescription("""
                    Lenzuola strappate pendono come ragnatele; al centro una pozza di ectoplasma fumante testimonia la presenza recentissima di Slimer.
                    Impronte lucide puntano verso l'ascensore: sembra che il fantasma abbia già cambiato area di gioco.
                       . A EST c'è il Corridoio Sud\
                    """
                )
                .build());

        gameMap.addRoom(new Room.RoomBuilder("room_1204")
                .setDisplayName("Stanza 1204")
                .setDescription("Una coppia sbalordita ti fissa indignata: evidentemente non gradiscono visite.")
                .setLongDescription("""
                    Champagne semi-versato, luci soffuse e risatine soffocate: niente di paranormale, solo imbarazzo umano al 100 %.
                    Ora dovresti proprio andar via...
                       . A OVEST c'è il Corridoio Sud\
                    """
                )
                .build());

        gameMap.addRoom(new Room.RoomBuilder("room_1205")
                .setDisplayName("Stanza 1205")
                .setDescription("La 1205 profuma di detergente fresco; la Signora delle pulizie è qui, pallida e tremante.")
                .setLongDescription("""
                    La signora delle pulizie è accovacciata accanto all'armadio, stringendo la scopa come se fosse l'ultima barriera tra lei e il caos ectoplasmatico; i suoi occhi ti seguono, pieni di paura.
                       . A EST c'è il Corridoio Sud Est\
                    """
                )
                .build());

        gameMap.addRoom(new Room.RoomBuilder("room_1206")
                .setDisplayName("Stanza 1206")
                .setDescription("La 1206 sembra vuota, a parte un mini-frigo lasciato in mezzo al letto.")
                .setLongDescription("""
                    L’arredamento è intatto, perfino elegante, ma il frigo vibra piano come se al suo interno bollisse qualcosa di poco sano. Aprirlo potrebbe essere il tuo ultimo atto da vivo… o rivelare lo snack peggiore di sempre.
                       . A OVEST c'è il Corridoio Sud Est\
                    """
                )
                .build());

        // Corridoio Sud Ovest
        gameMap.addRoom(new Room.RoomBuilder("hallway_12_sw")
                .setDisplayName("Corridoio Sud Ovest")
                .setDescription("La moquette logora attutisce i passi mentre il corridoio sud ovest si allunga davanti a te.")
                .setLongDescription("""
                    Alla tua sinistra la 1201 sembra respirare diffidenza; a destra la 1202 stilla melma dal telaio della porta. Le luci oscillano sospese tra voler morire e restare accese abbastanza da farti preoccupare.
                       . A NORD c'è il Corridoio Nord Ovest
                       . A OVEST c'è la Stanza 1201
                       . A EST  c'è la Stanza 1202\
                    """
                )
                .addLockedWestRoom(gameMap.getRoom("room_1201"), GameFlag.UNLOCK_ROOM_1201)
                .addLockedEastRoom(gameMap.getRoom("room_1202"), GameFlag.UNLOCK_ROOM_1202)
                .build()
        );

        // Corridoio Sud
        gameMap.addRoom(new Room.RoomBuilder("hallway_12_s")
                .setDisplayName("Corridoio Sud")
                .setDescription("Quadri storti e lampade tremolanti ti danno il benvenuto nel corridoio sud.")
                .setLongDescription("""
                    Davanti a te, le porte gemelle 1203 e 1204. Un carrello di servizio rovesciato blocca parzialmente il passaggio, riempiendo l'aria di odore di ammorbidente bruciato.
                       . A NORD c'è il Corridoio Ascensore
                       . A OVEST c'è la Stanza 1203
                       . A EST  c'è la Stanza 1204\
                    """
                )
                .addLockedWestRoom(gameMap.getRoom("room_1203"), GameFlag.UNLOCK_ROOM_1203)
                .addLockedEastRoom(gameMap.getRoom("room_1204"), GameFlag.UNLOCK_ROOM_1204)
                .build()
        );

        // Corridoio Sud Est
        gameMap.addRoom(new Room.RoomBuilder("hallway_12_se")
                .setDisplayName("Corridoio Sud Est")
                .setDescription("Un brivido gelido ti corre lungo la schiena mentre la melma illumina debolmente le pareti del corridoio sud est.")
                .setLongDescription("""
                    Il muro di fronte è coperto da un murale fluorescente di ectoplasma; gocce viscide colano sul tappeto imbibendolo di verde. La 1205 è sprangata, la 1206 è aperta di uno spiraglio e l’eco di una risata si perde verso ovest.
                       . A NORD c'è il Corridoio Nord Est
                       . A OVEST c'è la Stanza 1205
                       . A EST  c'è la Stanza 1206\
                    """
                )
                .addLockedWestRoom(gameMap.getRoom("room_1205"), GameFlag.UNLOCK_ROOM_1205)
                .addEastRoom(gameMap.getRoom("room_1206"))
                .build()
        );

        // Corridoio Nord Ovest
        gameMap.addRoom(new Room.RoomBuilder("hallway_12_nw")
                .setDisplayName("Corridoio Nord Ovest")
                .setDescription("La penombra del corridoio nord ovest avvolge una statua polverosa che sembra seguirti con lo sguardo.")
                .setLongDescription("""
                    Sulla guancia di marmo del cherubino scivola una lacrima di melma verdastra che termina in un blob lucente ai tuoi piedi. Un odore dolciastro permea l'aria, promemoria che qui è nascosta più di una semplice decorazione.
                       . A SUD c'è il Corridoio Sud Ovest
                       . A EST c'è il Corridoio Ascensore\
                    """
                )
                .addHiddenItem(new Item("key_1205", "Chiave della camera 1205", """
                        Ancora un po’ appiccicosa di melma verde, ma utilizzabile.\
                        """),
                        GameFlag.HALLWAY_12_NW_CLEAN
                )
                .addSouthRoom(gameMap.getRoom("hallway_12_sw"))
                .build());

        // Corridoio Nord Est
        gameMap.addRoom(new Room.RoomBuilder("hallway_12_ne")
                .setDisplayName("Corridoio Nord Est")
                .setDescription("Neon pallidi rimbalzano sulle chiazze di melma lungo il corridoio nord est.")
                .setLongDescription("""
                    Ogni macchia fluorescente è un passo della fuga di Slimer, e l'odore zuccherino di marshmallow andati a male impregna la moquette.
                       . A SUD c'è il Corridoio Sud Est
                       . A OVEST c'è il Corridoio Ascensore\
                    """
                )
                .addSouthRoom(gameMap.getRoom("hallway_12_se"))
                .build());

        // Corridoio Ascensore (12°)
        gameMap.addRoom(new Room.RoomBuilder("hallway_12_elevator")
                .setDisplayName("Corridoio Ascensore")
                .setDescription("Ti trovi nei corridoi del dodicesimo piano, vicino agli ascensori.")
                .setLongDescription("""
                    Ciò che resta del carrello della signora delle pulizie è un ammasso carbonizzato: qualcuno ha messo alla prova il raggio protonico qui. 
                    Le porte d’ottone degli ascensori scintillano ancora, in attesa.
                       . A NORD c'è l'Ascensore
                       . A SUD c'è il Corridoio Sud
                       . A EST c'è il Corridoio Nord Est
                       . A OVEST c'è il Corridoio Nord Ovest\
                    """
                )
                .addLockedSouthRoom(gameMap.getRoom("hallway_12_s"), GameFlag.TUTORIAL_PROTON_BACKPACK_COMPLETE)
                .addLockedEastRoom(gameMap.getRoom("hallway_12_ne"), GameFlag.TUTORIAL_PROTON_BACKPACK_COMPLETE)
                .addLockedWestRoom(gameMap.getRoom("hallway_12_nw"), GameFlag.TUTORIAL_PROTON_BACKPACK_COMPLETE)
                .addLockedRoom("north", "12", gameMap.getRoom("elevator"), GameFlag.ELEVATOR_OPEN)
                .addHiddenItem(new Item("mocio", "Mocio", "Utile per pulire la sporcizia"),
                        GameFlag.TUTORIAL_PROTON_BACKPACK_COMPLETE
                        )
                .build()
        );

        /* ----------------------------- Piano terra ------------------------ */
        // Sala da ballo
        gameMap.addRoom(new Room.RoomBuilder("ballroom")
                .setDisplayName("Sala da ballo")
                .setDescription("Un lampadario di cristallo ti accoglie nella sala da ballo scintillante e pericolosamente tranquilla.")
                .setLongDescription("""
                    Tavoli rotondi imbanditi, sedie Luigi XVI e un palco deserto attendono l’inizio di uno spettacolo paranormal-burlesque. I riflessi sulle grandi vetrate creano illusioni di ombre danzanti pronti a prendere vita al primo urlo.
                       . A EST c'è la Hall\
                    """
                )
                .build());

        // Sala ascensori (piano terra)
        gameMap.addRoom(new Room.RoomBuilder("hall_elevator")
                .setDisplayName("Sala degli ascensori")
                .setDescription("Pannelli in legno lucido e musica d'attesa fin troppo rilassante riempiono l'aria della sala degli ascensori.")
                .setLongDescription("""
                    Gli ascensori scorrono dietro porte d’ottone levigato. Dal corridoio verso la hall arriva l’eco del direttore in preda all’ansia.
                       . A NORD c'è l'Ascensore (piano terra)
                       . A SUD c'è la Hall\
                    """
                )
                .addLockedRoom("north", "pt", gameMap.getRoom("elevator"), GameFlag.ELEVATOR_OPEN)
                .build()
        );

        // Hall principale
        gameMap.addRoom(new Room.RoomBuilder("hall")
                .setDisplayName("Hall")
                .setDescription("Ti trovi nella hall dell'hotel.")
                .setLongDescription("""
                    Un albergo elegante, con moquette assassina e camerieri che si muovono come se niente stesse per andare storto. Illusi.
                    L’aria sa di cera, cocktail troppo agitati e “speriamo non succeda niente di strano oggi”.
                       . A NORD c'è la sala degli ascensori
                       . A EST  c'è la sala da ballo
                       . A SUD  c'è l'uscita\
                    """
                )
                .addLockedEastRoom(gameMap.getRoom("ballroom"), GameFlag.GHOST_BALLROOM)
                .addNorthRoom(gameMap.getRoom("hall_elevator"))
                .build()
        );
    }

    /** Registra tutti i {@link CommandHandler} e i loro alias. */
    private void createGameCommands() {
        addCommand(new ChiamaCommand(),   "chiama");
        addCommand(new GuardaCommand(),   "guarda", "guardati", "osserva");
        addCommand(new InventarioCommand(),"inventario");
        addCommand(new ParlaCommand(),    "parla");
        addCommand(new RaccogliCommand(), "raccogli", "prendi");
        addCommand(new UsaCommand(),      "usa", "utilizza");
        addCommand(new VaiCommand(),      "vai", "cammina");

        addCommand((c, gd, gs, a) -> List.of(new CommandHandler.CommandResponse(
                new GameNarratorPacket("""
                        Elenco comandi:
                         chiama               – L'ascensore, non puoi attraversare le porte chiuse.
                         osserva              – Descrive l’ambiente o un oggetto nel dettaglio.
                         inventario           – Elenca ciò che porti con te (zaino protonico compreso).
                         parla                – Avvia un dialogo con un personaggio presente.
                         raccogli / prendi    – Afferra un oggetto nelle vicinanze.
                         usa / utilizza       – Impiega un oggetto dell’inventario o dell’ambiente.
                         vai / cammina <dir>  – Muoviti: nord, sud, ovest, est...
                         aiuto                – Mostra questa schermata brillante e utile.\
                        """),
                false
        )), "aiuto", "help", "comandi");
    }

    /**
     * Associa un {@link CommandHandler} a uno o più alias testuali.
     *
     * @param commandHandler handler da registrare
     * @param aliases        varianti testuali che lo attivano
     */
    private void addCommand(CommandHandler commandHandler, String... aliases) {
        Arrays.stream(aliases).forEach(alias -> gameCommands.put(alias, commandHandler));
    }
}
