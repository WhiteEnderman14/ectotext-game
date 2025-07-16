package org.javamale.ectotext.client.network;

import org.javamale.ectotext.client.GameClient;
import org.javamale.ectotext.common.packet.Packet;

import java.io.*;
import java.net.Socket;

/**
 * Client socket per la comunicazione TCP/IP con il server di gioco.
 * Questa classe gestisce:
 * <ul>
 *   <li>Connessione TCP/IP con il server</li>
 *   <li>Invio di pacchetti al server in formato JSON</li>
 *   <li>Ricezione e gestione dei messaggi dal server</li>
 *   <li>Chiusura sicura della connessione</li>
 * </ul>
 * <p>
 * La classe estende Thread per gestire la ricezione asincrona dei messaggi.
 * </p>
 */
public class SocketClient extends Thread {
    /** Socket per la comunicazione TCP/IP con il server. */
    private final Socket socket;

    /** Reader per la ricezione dei messaggi dal server. */
    private final BufferedReader in;

    /** Writer per l'invio dei messaggi al server. */
    private final PrintWriter out;

    /**
     * Crea un nuovo client socket e stabilisce la connessione con il server.
     * <p>
     * Inizializza i buffer di input e output per la comunicazione.
     * </p>
     *
     * @param host indirizzo del server a cui connettersi
     * @param port porta del server a cui connettersi
     * @throws IOException se la connessione fallisce o si verificano errori
     *                     nell'inizializzazione dei buffer
     */
    public SocketClient(String host, int port) throws IOException {
        socket = new Socket(host, port);

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
    }

    /**
     * Invia un pacchetto al server in formato JSON.
     * <p>
     * Il metodo è thread-safe grazie alla sincronizzazione sul writer.
     * Il pacchetto viene convertito in JSON prima dell'invio.
     * </p>
     *
     * @param packet il pacchetto da inviare al server
     * @throws NullPointerException se packet è null
     */
    public void sendMessage(Packet packet) {
        synchronized (out){
            String json = packet.toBaseJson();
            out.println(json);
        }
    }

    /**
     * Chiude la connessione socket e interrompe il thread di ricezione.
     * <p>
     * Il metodo è sincronizzato per garantire una chiusura sicura
     * della connessione anche in presenza di più thread.
     * </p>
     *
     * @throws RuntimeException se si verifica un errore durante la chiusura del socket
     */
    public synchronized void close() {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.interrupt();
    }

    /**
     * Esegue il ciclo principale di ricezione dei messaggi dal server.
     * Il thread:
     * <ul>
     *   <li>Legge continuamente messaggi JSON dal server</li>
     *   <li>Converte i messaggi in oggetti Packet</li>
     *   <li>Inoltra i pacchetti al GameClient per l'elaborazione</li>
     *   <li>Gestisce la disconnessione in caso di errori o chiusura del server</li>
     * </ul>
     *
     * @see Packet#fromBaseJson(String)
     * @see GameClient#handleUpdate(Packet)
     */
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String json = in.readLine();

                    if (json == null) {
                        GameClient.getInstance().disconnect();
                        break;
                    }

                    Packet update = Packet.fromBaseJson(json);
                    GameClient.getInstance().handleUpdate(update);

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