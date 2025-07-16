package org.javamale.ectotext.server.network;

import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.server.GameServer;
import org.javamale.ectotext.server.contracts.UpdateHandler;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * Gestore della comunicazione con un singolo client connesso al server.
 * Questa classe implementa un handler multi-thread che:
 * <ul>
 *   <li>Gestisce la comunicazione bidirezionale con un client specifico</li>
 *   <li>Serializza/deserializza i pacchetti in formato JSON</li>
 *   <li>Notifica gli eventi di comunicazione attraverso un {@link UpdateHandler}</li>
 *   <li>Gestisce la disconnessione ordinata del client</li>
 * </ul>
 *
 * @see UpdateHandler
 * @see Packet
 * @see Socket
 */
public class ClientHandler extends Thread {

    /**
     * Socket TCP per la comunicazione con il client.
     * Viene chiuso quando la connessione termina o il client si disconnette.
     */
    private final Socket socket;

    /**
     * Handler per la gestione degli eventi di comunicazione.
     * Riceve notifiche per pacchetti in arrivo e disconnessioni.
     */
    private UpdateHandler updateHandler;

    /**
     * Stream di input bufferizzato per la lettura dei messaggi dal client.
     * Utilizzato per leggere i pacchetti JSON line by line.
     */
    private final BufferedReader in;

    /**
     * Stream di output bufferizzato per l'invio dei messaggi al client.
     * Configurato per l'auto-flush dopo ogni messaggio.
     */
    private final PrintWriter out;

    /**
     * Crea un nuovo handler per gestire la comunicazione con un client.
     * <p>
     * Inizializza gli stream di I/O bufferizzati per la comunicazione
     * efficiente dei pacchetti JSON.
     * </p>
     *
     * @param socket socket TCP connesso al client
     * @param updateHandler handler per la gestione degli eventi di comunicazione
     * @throws IOException se si verificano errori nell'inizializzazione degli stream
     * @throws NullPointerException se socket o updateHandler sono null
     */
    public ClientHandler(Socket socket, UpdateHandler updateHandler) throws IOException {
        this.socket = socket;
        this.updateHandler = updateHandler;

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
    }

    /**
     * Aggiorna l'handler degli eventi di comunicazione.
     * <p>
     * Questo metodo permette di modificare dinamicamente la gestione
     * degli eventi durante la vita del client, ad esempio quando
     * il client si sposta tra diverse stanze di gioco.
     * </p>
     *
     * @param updateHandler nuovo handler per gli eventi
     * @throws NullPointerException se updateHandler è null
     */
    public void setUpdateHandler(UpdateHandler updateHandler) {
        this.updateHandler = updateHandler;
    }

    /**
     * Invia un pacchetto al client.
     * <p>
     * Il pacchetto viene serializzato in JSON e inviato in modo thread-safe.
     * L'invio è sincronizzato per evitare interferenze tra thread multipli.
     * </p>
     *
     * @param packet pacchetto da inviare
     * @throws NullPointerException se packet è null
     * @see Packet#toBaseJson()
     */
    public void sendMessage(Packet packet) {
        synchronized (out) {
            String json = packet.toBaseJson();
            out.println(json);
        }
    }

    /**
     * Chiude la connessione con il client.
     * Questo metodo:
     * <ul>
     *   <li>Chiude il socket TCP</li>
     *   <li>Interrompe il thread di ascolto</li>
     *   <li>Libera le risorse associate</li>
     * </ul>
     *
     * @throws RuntimeException se si verifica un errore durante la chiusura
     */
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.interrupt();
    }

    /**
     * Loop principale di gestione della comunicazione.
     * Questo metodo:
     * <ul>
     *   <li>Legge continuamente pacchetti JSON dal client</li>
     *   <li>Deserializza i pacchetti ricevuti</li>
     *   <li>Notifica l'UpdateHandler per ogni pacchetto</li>
     *   <li>Gestisce disconnessioni e errori di comunicazione</li>
     * </ul>
     *
     * Il loop termina quando:
     * <ul>
     *   <li>Il client si disconnette (EOF sullo stream)</li>
     *   <li>Si verifica un errore di socket</li>
     *   <li>Il thread viene interrotto</li>
     * </ul>
     *
     * @see Thread#run()
     * @see UpdateHandler#onUpdate(ClientHandler, Packet)
     * @see UpdateHandler#onDisconnect(ClientHandler)
     */
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String json = in.readLine();

                    if (json == null) {
                        updateHandler.onDisconnect(this);
                        GameServer.getSocketServer().disconnectClient(this);
                        break;
                    }

                    Packet update = Packet.fromBaseJson(json);
                    updateHandler.onUpdate(this, update);
                } catch (SocketException e) {
                    System.err.println(e.getMessage());
                    updateHandler.onDisconnect(this);
                    GameServer.getSocketServer().disconnectClient(this);
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}