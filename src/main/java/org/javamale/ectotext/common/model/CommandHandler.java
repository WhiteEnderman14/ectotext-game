package org.javamale.ectotext.common.model;

import org.javamale.ectotext.common.packet.Packet;

import java.util.List;

/**
 * Gestisce l’esecuzione di un comando proveniente da un {@link Character}.
 * <p>
 * Un’implementazione di {@code CommandHandler} analizza gli argomenti ricevuti,
 * modifica lo stato di gioco se necessario e produce una o più risposte
 * ({@link CommandResponse}) da inviare al client o a tutti i giocatori.
 * </p>
 */
public interface CommandHandler {

    /**
     * Esegue il comando richiesto dal giocatore.
     *
     * @param character      il personaggio che ha emesso il comando
     * @param gameDescription descrizione statica del gioco (mappe, comandi, ecc.)
     * @param gameState      stato dinamico attuale della partita
     * @param args           argomenti del comando (token già suddivisi)
     * @return una lista di {@link CommandResponse} ciascuna contenente un {@link Packet}
     *         da inviare e un flag che indica se il pacchetto deve essere
     *         diffuso (“broadcast”) a tutti i giocatori o solo al mittente
     */
    List<CommandResponse> execute(Character character,
                                  GameDescription gameDescription,
                                  GameState gameState,
                                  String[] args);

    // ---------------------------------------------------------------------
    //                             INNER CLASS
    // ---------------------------------------------------------------------

    /**
     * Rappresenta la risposta all’esecuzione di un comando.
     * Contiene il pacchetto di rete da inviare e l’indicazione
     * se lo stesso deve essere inviato a tutti oppure al solo giocatore che
     * ha eseguito il comando.
     */
    class CommandResponse {

        /** Pacchetto da inviare al/ai client. */
        private final Packet packet;

        /** {@code true} se il pacchetto va trasmesso a tutti i giocatori, {@code false} se solo al mittente. */
        private final boolean broadcast;

        /**
         * Costruisce una risposta di comando.
         *
         * @param packet    pacchetto di rete da spedire
         * @param broadcast {@code true} per broadcast a tutti, {@code false} per risposta singola
         */
        public CommandResponse(Packet packet, boolean broadcast) {
            this.packet = packet;
            this.broadcast = broadcast;
        }

        /**
         * Restituisce il pacchetto da inviare.
         *
         * @return pacchetto di rete
         */
        public Packet getPacket() {
            return packet;
        }

        /**
         * Indica se il pacchetto deve essere inviato in broadcast.
         *
         * @return {@code true} se broadcast, {@code false} altrimenti
         */
        public boolean isBroadcast() {
            return broadcast;
        }
    }
}
