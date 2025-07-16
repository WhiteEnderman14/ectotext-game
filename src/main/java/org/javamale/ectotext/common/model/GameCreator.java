package org.javamale.ectotext.common.model;

import org.javamale.ectotext.common.packet.Packet;

import java.util.List;

/**
 * Definisce il contratto per la creazione di un nuovo gioco.
 * <p>
 * Un’implementazione di {@code GameCreator} si occupa di:
 * <ul>
 *   <li>costruire la <strong>descrizione statica</strong> del gioco
 *       ({@link #createGameDescription()}) che include mappa, stanze,
 *       oggetti, comandi disponibili, ecc.;</li>
 *   <li>generare lo <strong>stato iniziale</strong> della partita
 *       ({@link #createDefaultGameState(GameDescription)}) a partire
 *       dalla descrizione;</li>
 *   <li>fornire la sequenza di pacchetti di <strong>introduzione</strong>
 *       che viene inviata ai client quando la partita inizia
 *       ({@link #gameIntro()}).</li>
 * </ul>
 */
public interface GameCreator {

    /**
     * Crea la descrizione statica del gioco.
     * <p>
     * Questo oggetto contiene tutte le informazioni immutabili:
     * stanze, direzioni possibili, comandi registrati, NPC di default, ecc.
     *
     * @return una nuova {@link GameDescription} pronta per essere usata
     *         nella fase di configurazione iniziale
     */
    GameDescription createGameDescription();

    /**
     * Costruisce lo stato di gioco di default basandosi sulla descrizione.
     * <p>
     * In questa fase vengono posizionate le entità dinamiche iniziali
     * (personaggi, oggetti, flag di gioco) e impostati eventuali timer o
     * contatori. Viene invocato subito dopo
     * {@link #createGameDescription()}.
     *
     * @param gameDescription descrizione statica generata in precedenza
     * @return {@link GameState} iniziale pronto per avviare la partita
     */
    GameState createDefaultGameState(GameDescription gameDescription);

    /**
     * Restituisce l’introduzione della partita sotto forma di pacchetti
     * di rete da inviare ai client.
     * <p>
     * Può includere testo narrativo, messaggi di benvenuto, regole base o
     * qualsiasi informazione da mostrare ai giocatori prima dell’inizio
     * effettivo del gioco.
     *
     * @return lista di {@link Packet} da inviare in successione ai client
     */
    List<Packet> gameIntro();
}
