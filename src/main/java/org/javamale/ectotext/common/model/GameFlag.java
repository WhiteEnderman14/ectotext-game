package org.javamale.ectotext.common.model;

import java.util.Arrays;

/**
 * Gestisce i flag di stato del gioco che influenzano la logica e il flusso narrativo.
 * <p>
 * I flag sono suddivisi in categorie logiche:
 * <ul>
 *   <li><strong>NPC</strong> - Stati relativi ai personaggi non giocanti</li>
 *   <li><strong>Stanze</strong> - Accessi e modifiche alle aree di gioco</li>
 *   <li><strong>First Time</strong> - Eventi che si verificano solo una volta</li>
 *   <li><strong>Oggetti</strong> - Tracciamento degli oggetti raccolti</li>
 *   <li><strong>Tutorial</strong> - Progressione dell'addestramento</li>
 * </ul>
 * <p>
 * Ogni flag è identificato da una chiave testuale che segue il pattern:
 * {@code tipo.sottotipo.nome} per facilitare organizzazione e serializzazione.
 * 
 * @see GameState Classe che gestisce la collezione di flag attivi
 */
public enum GameFlag {

    /* -------------------------- NPC -------------------------- */
    /** Flag che indica la cattura completata del fantasma. */
    GHOST_CAPTURED("npc.ghost.captured"),
    
    /** Flag che indica la presenza del fantasma nella sala da ballo. */
    GHOST_BALLROOM("npc.ghost.ballroom"),
    
    /** Flag che indica il posizionamento della trappola per fantasmi. */
    GHOST_TRAP_PLACED("npc.ghost.trap_placed"),
    
    /** Flag che indica che il fantasma è immobilizzato sulla trappola. */
    GHOST_TRAP_READY("npc.ghost.trap_ready"),
    
    /** Flag che indica la presenza dell'ospite nella stanza 1202. */
    GUEST_1202("npc.guest_1202.in_room"),

    /* ------------------------- STANZE ------------------------ */
    /** Flag che indica che l'ascensore è utilizzabile dai giocatori. */
    ELEVATOR_OPEN("room.elevator.open"),

    /** Flag di sblocco per l'accesso alla stanza 1201. */
    UNLOCK_ROOM_1201("room.room_1201.unlocked"),
    
    /** Flag di sblocco per l'accesso alla stanza 1202. */
    UNLOCK_ROOM_1202("room.room_1202.unlocked"),
    
    /** Flag di sblocco per l'accesso alla stanza 1203. */
    UNLOCK_ROOM_1203("room.room_1203.unlocked"),
    
    /** Flag di sblocco per l'accesso alla stanza 1204. */
    UNLOCK_ROOM_1204("room.room_1204.unlocked"),
    
    /** Flag di sblocco per l'accesso alla stanza 1205. */
    UNLOCK_ROOM_1205("room.room_1205.unlocked"),

    /** Flag che indica che il corridoio nord-ovest è stato pulito. */
    HALLWAY_12_NW_CLEAN("room.hallway_12_nw.clean"),

    /* ------------------ EVENTI "FIRST TIME" ------------------ */
    /** Flag per la prima visita alla hall dell'hotel. */
    FIRST_TIME_HALL("room.hall.firsttime"),
    
    /** Flag per il primo utilizzo dell'ascensore. */
    FIRST_TIME_ELEVATOR("room.elevator.firsttime"),
    
    /** Flag per il primo accesso alla sala ascensori. */
    FIRST_TIME_HALL_ELEVATOR("room.hall_elevator.firsttime"),
    
    /** Flag per la prima visita al corridoio sud-est del 12° piano. */
    FIRST_TIME_HALLWAY_12_SE("room.hallway_12_se.firsttime"),
    
    /** Flag per la prima visita alla stanza 1202. */
    FIRST_TIME_ROOM_1202("room.room_1202.firsttime"),
    
    /** Flag per la prima visita alla stanza 1203. */
    FIRST_TIME_ROOM_1203("room.room_1203.firsttime"),
    
    /** Flag per il primo accesso alla sala da ballo. */
    FIRST_TIME_BALLROOM("room.ballroom.firsttime"),

    /* --------------------- OGGETTI RACCOLTI ------------------- */
    /** Flag che indica che il mocio è stato raccolto. */
    MOCIO_PICKED("room.hallway_12_elevator.item.mocio.collected"),
    
    /** Flag che indica che la chiave della stanza 1205 è stata raccolta. */
    KEY_1205("room.hallway_12_nw.item.key_1205.collected"),

    /* ------------------------ TUTORIAL ----------------------- */
    /** Flag che indica l'inizio del tutorial dello zaino protonico. */
    TUTORIAL_PROTON_BACKPACK("tutorial.use.proton_backpack"),
    
    /** Flag che indica il completamento del tutorial dello zaino protonico. */
    TUTORIAL_PROTON_BACKPACK_COMPLETE("tutorial.use.proton_backpack.complete"),

    /* --------------------------------------------------------- */
    ;

    /** Chiave univoca che identifica il flag nel sistema. */
    private final String key;

    /**
     * Costruisce un nuovo flag con la chiave specificata.
     *
     * @param key chiave univoca che identifica il flag nel formato tipo.sottotipo.nome
     */
    GameFlag(String key) {
        this.key = key;
    }

    /**
     * Restituisce la chiave testuale associata al flag.
     *
     * @return stringa nel formato tipo.sottotipo.nome
     */
    public String getKey() {
        return key;
    }

    /**
     * Cerca e restituisce il flag corrispondente alla chiave specificata.
     * <p>
     * Utile per convertire chiavi testuali (ad esempio da file di salvataggio
     * o network) nei corrispondenti valori enum.
     *
     * @param key chiave testuale da cercare
     * @return il flag corrispondente o null se non trovato
     */
    public static GameFlag fromKey(String key) {
        return Arrays.stream(GameFlag.values())
                .filter(x -> x.getKey().equals(key))
                .findFirst()
                .orElse(null);
    }
}