package org.javamale.ectotext.server.contracts;

import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.server.network.ClientHandler;

/**
 * Gestisce gli eventi di comunicazione tra client e server.
 * Questa interfaccia definisce il contratto per la gestione degli eventi di rete:
 * <ul>
 *   <li>Ricezione di pacchetti dai client</li>
 *   <li>Disconnessione dei client</li>
 * </ul>
 *
 * Le implementazioni di questa interfaccia devono garantire:
 * <ul>
 *   <li>Thread safety nella gestione degli eventi</li>
 *   <li>Gestione appropriata degli errori di comunicazione</li>
 *   <li>Elaborazione dei pacchetti secondo il protocollo</li>
 *   <li>Pulizia delle risorse in caso di disconnessione</li>
 * </ul>
 *
 * @see ClientHandler
 * @see Packet
 */
public interface UpdateHandler {

    /**
     * Elabora un pacchetto ricevuto da un client.
     * Questo metodo viene chiamato quando un client invia un pacchetto al server.
     * L'implementazione deve:
     * <ul>
     *   <li>Validare il pacchetto ricevuto</li>
     *   <li>Processare il contenuto secondo il protocollo</li>
     *   <li>Gestire eventuali errori di elaborazione</li>
     *   <li>Inviare risposte appropriate al client</li>
     * </ul>
     *
     * @param client handler del client mittente
     * @param update pacchetto ricevuto da elaborare
     */
    void onUpdate(ClientHandler client, Packet update);

    /**
     * Gestisce la disconnessione di un client.
     * Questo metodo viene chiamato quando:
     * <ul>
     *   <li>Il client si disconnette volontariamente</li>
     *   <li>La connessione viene persa</li>
     *   <li>Si verifica un errore fatale di comunicazione</li>
     * </ul>
     *
     * L'implementazione deve:
     * <ul>
     *   <li>Liberare le risorse associate al client</li>
     *   <li>Aggiornare lo stato del sistema</li>
     *   <li>Notificare gli altri client se necessario</li>
     * </ul>
     *
     * @param client handler del client disconnesso
     */
    void onDisconnect(ClientHandler client);
}
