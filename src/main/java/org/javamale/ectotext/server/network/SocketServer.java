package org.javamale.ectotext.server.network;

import org.javamale.ectotext.server.GameServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

/**
 * Server TCP multi-thread per la gestione delle connessioni dei client di gioco.
 * Questa classe implementa un server TCP che:
 * <ul>
 *   <li>Accetta connessioni in ingresso su una porta configurabile</li>
 *   <li>Gestisce multiple connessioni client simultanee</li>
 *   <li>Mantiene una lista thread-safe dei client attivi</li>
 *   <li>Coordina la disconnessione ordinata dei client</li>
 * </ul>
 * 
 * <p>
 * Il server utilizza un thread dedicato per l'accettazione delle connessioni
 * e crea un {@link ClientHandler} separato per ogni client connesso.
 * </p>
 *
 * @see ClientHandler
 * @see Thread
 */
public class SocketServer extends Thread {
    /** 
     * Porta TCP su cui il server accetta connessioni.
     * Questo valore è immutabile dopo la creazione del server.
     */
    private final int PORT;

    /** 
     * Socket server per l'accettazione delle nuove connessioni TCP.
     * @see ServerSocket
     */
    private final ServerSocket serverSocket;

    /** 
     * Set thread-safe dei client attualmente connessi.
     * Viene sincronizzato esplicitamente per le operazioni di modifica.
     */
    private final HashSet<ClientHandler> clients;

    /**
     * Crea un nuovo server TCP sulla porta specificata.
     *
     * @param port porta su cui il server deve accettare connessioni (1-65535)
     * @throws IOException se la porta è già in uso o non può essere aperta
     * @throws IllegalArgumentException se la porta non è valida
     */
    public SocketServer(int port) throws IOException {
        this.PORT = port;
        serverSocket = new ServerSocket(PORT);
        clients = new HashSet<>();
    }

    /**
     * Crea un nuovo server TCP sulla porta predefinita 6666.
     *
     * @throws IOException se la porta è già in uso o non può essere aperta
     * @see #SocketServer(int)
     */
    public SocketServer() throws IOException {
        this(6666);
    }

    /**
     * Ottiene la porta su cui il server è in ascolto.
     *
     * @return numero della porta TCP attiva
     */
    public int getPort() {
        return PORT;
    }

    /**
     * Arresta il server in modo ordinato.
     * Questo metodo:
     * <ol>
     *   <li>Chiude il socket server per impedire nuove connessioni</li>
     *   <li>Disconnette tutti i client attualmente connessi</li>
     *   <li>Interrompe il thread di accettazione</li>
     * </ol>
     *
     * @throws RuntimeException se si verifica un errore durante la chiusura
     */
    public void shutdown() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        disconnectAll();

        this.interrupt();
    }

    /**
     * Disconnette tutti i client attualmente connessi.
     * <p>
     * L'operazione è thread-safe e atomica: tutti i client vengono
     * disconnessi in un'unica transazione sincronizzata.
     * </p>
     */
    public void disconnectAll() {
        synchronized (clients) {
            clients.forEach(ClientHandler::close);
            clients.clear();
        }
    }

    /**
     * Disconnette un singolo client e lo rimuove dalla lista dei client attivi.
     * <p>
     * L'operazione è thread-safe e viene eseguita in modo atomico.
     * </p>
     *
     * @param client il client da disconnettere
     * @throws NullPointerException se client è null
     */
    public void disconnectClient(ClientHandler client) {
        synchronized (clients) {
            clients.remove(client);
            client.close();
        }
    }

    /**
     * Loop principale del server per l'accettazione delle connessioni.
     * Questo metodo:
     * <ul>
     *   <li>Accetta continuamente nuove connessioni TCP</li>
     *   <li>Crea un nuovo {@link ClientHandler} per ogni connessione</li>
     *   <li>Gestisce gli errori per connessioni singole</li>
     *   <li>Si interrompe in modo pulito quando il thread viene interrotto</li>
     * </ul>
     *
     * @see Thread#run()
     * @see ClientHandler
     */
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()){
                try {
                    Socket socket = serverSocket.accept();

                    ClientHandler clientHandler = new ClientHandler(socket, GameServer.getRoomManager());
                    clientHandler.start();

                    synchronized (clients) {
                        clients.add(clientHandler);
                    }

                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}