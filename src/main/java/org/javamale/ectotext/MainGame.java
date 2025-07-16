package org.javamale.ectotext;

import org.javamale.ectotext.client.GameClient;
import org.javamale.ectotext.server.GameServer;
import org.javamale.ectotext.server.persistence.DbManager;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Punto di ingresso principale dell'applicazione "Incubo al Sedgewick Hotel".
 * Questa classe gestisce l'avvio dell'applicazione in due modalità:
 * <ul>
 *   <li>Modalità Client: avvia l'interfaccia grafica per i giocatori</li>
 *   <li>Modalità Server: avvia il server di gioco con configurazione personalizzabile</li>
 * </ul>
 * Utilizza picocli per la gestione dei comandi da riga di comando.
 */
@CommandLine.Command(
        mixinStandardHelpOptions = true,
        versionProvider = MainGame.VersionProvider.class,
        description = """
        "Incubo al Sedgewick Hotel" is a multiplayer text adventure game set in a haunted hotel.
        
        This application can be launched in two modes:
          - Client mode: starts the graphical interface for players to explore rooms, interact with the environment, and communicate with others.
          - Server mode: runs the game server, handles game state, player data, and room logic.
          Supports JSON room loading, database reset, and configuration via properties file.

        Use 'client' or 'server' subcommands to choose the mode.
        """,
        subcommands = {MainGame.ClientCommand.class, MainGame.ServerCommand.class}
)
public class MainGame implements Runnable {
    /** Versione corrente dell'applicazione. */
    public static final String VERSION = "1.0.0";

    /**
     * Punto di ingresso principale dell'applicazione.
     * Gestisce il parsing degli argomenti e l'avvio della modalità appropriata.
     *
     * @param args argomenti della riga di comando
     */
    public static void main(String[] args) {
        new CommandLine(new MainGame()).execute(args);
    }

    /**
     * Esegue il comportamento predefinito dell'applicazione (modalità client).
     */
    @Override
    public void run() {
        System.out.println("Launching in CLIENT mode (default)");
        GameClient.getInstance().startWindow();
    }

    /**
     * Provider per la versione dell'applicazione utilizzato da picocli.
     */
    public static class VersionProvider implements CommandLine.IVersionProvider {
        /**
         * Restituisce la stringa di versione dell'applicazione.
         *
         * @return array contenente la stringa di versione
         */
        @Override
        public String[] getVersion() {
            return new String[] { "Incubo al Sedgewick Hotel v" + VERSION };
        }
    }

    /**
     * Comando per l'avvio in modalità client.
     */
    @CommandLine.Command(
            mixinStandardHelpOptions = true,
            versionProvider = MainGame.VersionProvider.class,
            name = "client",
            description = "Launch the client")
    static class ClientCommand implements Runnable {
        /**
         * Avvia l'applicazione in modalità client con interfaccia grafica.
         */
        @Override
        public void run() {
            System.out.println("Launching in CLIENT mode");
            GameClient.getInstance().startWindow();
        }
    }

    /**
     * Comando per l'avvio in modalità server con opzioni di configurazione.
     */
    @CommandLine.Command(
            mixinStandardHelpOptions = true,
            versionProvider = MainGame.VersionProvider.class,
            name = "server",
            description = "Launch the server with options"
    )
    static class ServerCommand implements Runnable {
        /** Latch per la gestione dello shutdown del server. */
        private final CountDownLatch shutdownLatch = new CountDownLatch(1);

        /** File di configurazione delle proprietà del server. */
        @CommandLine.Option(names = {"-p", "--properties"}, description = "Path to properties file")
        private File propertiesFile;

        /** File JSON contenente la definizione delle stanze. */
        @CommandLine.Option(names = {"-r", "--rooms"}, description = "Path to rooms.json file")
        private File roomsFile;

        /** Porta per il server socket. */
        @CommandLine.Option(names = "--socket-port", description = "Socket port")
        private Integer socketPort;

        /** Porta per il server REST. */
        @CommandLine.Option(names = "--rest-port", description = "REST port")
        private Integer restPort;

        /** Flag per il reset del database. */
        @CommandLine.Option(names = "--reset-db", description = "Reset the database")
        private boolean resetDb;

        /**
         * Avvia il server con la configurazione specificata.
         * <p>
         * Il metodo:
         * <ul>
         *   <li>Carica le configurazioni dal file properties se specificato</li>
         *   <li>Inizializza il server sulle porte specificate</li>
         *   <li>Carica le stanze dal file JSON se specificato</li>
         *   <li>Gestisce il reset del database se richiesto</li>
         *   <li>Configura lo shutdown hook per la chiusura pulita del server</li>
         * </ul>
         * </p>
         */
        @Override
        public void run() {
            Properties props = new Properties();

            if (propertiesFile != null) {
                try (FileInputStream fis = new FileInputStream(propertiesFile)) {
                    props.load(fis);
                } catch (IOException e) {
                    System.err.println("Error: Unable to read properties file: " + propertiesFile);
                    System.exit(1);
                }
            }

            if (socketPort == null && props.getProperty("socket-port") != null) {
                try {
                    socketPort = Integer.parseInt(props.getProperty("socket-port"));
                } catch (NumberFormatException e) {
                    System.err.println("Error: Invalid socket port value in properties file");
                    System.exit(1);
                }
            }

            if (restPort == null && props.getProperty("rest-port") != null) {
                try {
                    restPort = Integer.parseInt(props.getProperty("rest-port"));
                } catch (NumberFormatException e) {
                    System.err.println("Error: Invalid REST port value in properties file");
                    System.exit(1);
                }
            }

            System.out.println("Launching in SERVER mode");

            // Set default ports if not provided
            socketPort = socketPort == null ? 6666 : socketPort;
            restPort = restPort == null ? 8080 : restPort;

            try {
                GameServer.init(socketPort, restPort);
            } catch (IOException e) {
                System.err.println("Error: Failed to start the server");
                System.err.println(e.getMessage());
                System.exit(1);
            }

            int loadedRooms = 0;

            if (roomsFile != null) {
                try {
                    loadedRooms = GameServer.loadGameRoomsFromJson(roomsFile);
                } catch (IOException e) {
                    System.err.println("Error: Failed to load rooms from JSON file: " + roomsFile);
                    System.err.println(e.getMessage());
                }
            }

            if (resetDb) {
                try {
                    DbManager.resetDb();
                    System.out.println("Database reset completed. Please restart the server.");
                } catch (Exception e) {
                    System.err.println("Error while resetting the database:");
                    System.err.println(e.getMessage());
                }
                GameServer.shutdown();
                System.exit(0);
            }

            System.out.println("Server started successfully");
            System.out.println("REST API listening on port: " + restPort);
            System.out.println("Socket server listening on port: " + socketPort);

            if (loadedRooms > 0) {
                System.out.println("Loaded " + loadedRooms + " room(s) from: " + roomsFile);
            }

            /*
             * Shutdown hook: invoked when the JVM receives Ctrl + C (SIGINT) or SIGTERM.
             * Closes the server gracefully.
             */
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n⏹  Stopping server…");
                GameServer.shutdown();
                System.out.println("Server stopped successfully.");
                shutdownLatch.countDown();          // releases the main thread
            }));

            // Keep the JVM alive until the hook fires
            try {
                shutdownLatch.await();              // waits for Ctrl + C
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
}