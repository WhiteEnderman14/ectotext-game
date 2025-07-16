package org.javamale.ectotext.server.persistence;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe DbManager.
 * <p>
 * Responsabilità principale di DbManager: gestire la connessione al database H2,
 * assicurare l’unicità dell’istanza tramite pattern Singleton e creare le tabelle
 * al primo avvio se il database non esiste.
 * </p>
 */
public class DbManager {
    /**
     * Istanza singleton di DbManager.
     */
    private static volatile DbManager instance;

    /**
     * Connessione al database.
     */
    private final Connection con;
    /**
     * Percorso base del database.
     */
    private static final String DB_PATH = "./ectotextdb";
    /**
     * File fisico del database H2.
     */
    private static final String DB_FILE = DB_PATH + ".mv.db";
    /**
     * File di trace del database H2.
     */
    private static final String DB_FILE_TRACE = DB_PATH + ".trace.db";

    /**
     * Costruttore privato per Singleton. Inizializza la connessione e crea le tabelle se necessario.
     *
     * @throws SQLException se la connessione o il setup fallisce
     */
    private DbManager() throws SQLException {
        String url = "jdbc:h2:" + DB_PATH;
        String user = "sa";
        String password = "";

        boolean dbExists = new File(DB_FILE).exists();

        con = DriverManager.getConnection(url, user, password);

        if (!dbExists) {
            setupDatabase();
        }
    }

    /**
     * Restituisce l’istanza Singleton di DbManager.
     *
     * @return istanza di DbManager
     */
    public static DbManager getInstance() {
        if (instance == null) {
            synchronized (DbManager.class) {
                if (instance == null) {
                    try {
                        instance = new DbManager();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return instance;
    }

    /**
     * Restituisce la Connection singleton a H2.
     *
     * @return oggetto Connection condiviso
     */
    public static Connection getConnection() {
        return getInstance().getCon();
    }

    /**
     * Restituisce la Connection.
     *
     * @return valore di tipo Connection
     */
    public Connection getCon() {
        return con;
    }

    /**
     * Esegue la creazione delle tabelle del database (se non esistono già).
     *
     * @throws SQLException se la creazione delle tabelle fallisce
     */
    public void setupDatabase() throws SQLException {
        String sqlCreateTables = """
            CREATE TABLE IF NOT EXISTS game_rooms (
                gr_name VARCHAR(30) PRIMARY KEY,
                gr_password VARCHAR(30) NOT NULL
            );
            
            CREATE TABLE IF NOT EXISTS game_states (
                id UUID PRIMARY KEY,
                gr_name VARCHAR(30),
                FOREIGN KEY (gr_name) REFERENCES game_rooms(gr_name) ON UPDATE CASCADE ON DELETE CASCADE
            );
            
            CREATE TABLE IF NOT EXISTS game_flags (
                game_state_id UUID NOT NULL,
                flag_key VARCHAR(100) NOT NULL,
                PRIMARY KEY (game_state_id, flag_key),
                FOREIGN KEY (game_state_id) REFERENCES game_states(id) ON UPDATE CASCADE ON DELETE CASCADE
            );
            
            CREATE TABLE IF NOT EXISTS characters (
                game_state_id UUID NOT NULL,
                character_name VARCHAR(100) NOT NULL,
                character_display_name VARCHAR(100) NOT NULL,
                character_current_room VARCHAR(100) NOT NULL,
                PRIMARY KEY (game_state_id, character_name),
                FOREIGN KEY (game_state_id) REFERENCES game_states(id) ON UPDATE CASCADE ON DELETE CASCADE
            );
            
            CREATE TABLE IF NOT EXISTS npcs (
                game_state_id UUID NOT NULL,
                npc_name VARCHAR(100) NOT NULL,
                npc_display_name VARCHAR(100) NOT NULL,
                npc_current_room VARCHAR(100) NOT NULL,
                PRIMARY KEY (game_state_id, npc_name),
                FOREIGN KEY (game_state_id) REFERENCES game_states(id) ON UPDATE CASCADE ON DELETE CASCADE
            );
            
            CREATE TABLE IF NOT EXISTS character_inventories (
                game_state_id UUID NOT NULL,
                character_name VARCHAR(100) NOT NULL,
                item_name VARCHAR(100) NOT NULL,
                item_display_name VARCHAR(100) NOT NULL,
                item_description VARCHAR(100) NOT NULL,
                PRIMARY KEY (game_state_id, character_name, item_name),
                FOREIGN KEY (game_state_id, character_name) REFERENCES characters(game_state_id, character_name) ON UPDATE CASCADE ON DELETE CASCADE
            );
            """;
        Statement stmt = con.createStatement();
        stmt.executeUpdate(sqlCreateTables);
    }

    /**
     * Elimina fisicamente il file del database (usato per reset).
     * Se il database esiste e non è eliminabile, viene sollevata una RuntimeException.
     */
    public synchronized static void resetDb() {
        File dbFile = new File(DB_FILE);
        File dbFileTrace = new File(DB_FILE_TRACE);
        if (dbFile.exists() && !dbFile.delete() && dbFileTrace.exists() && !dbFileTrace.delete()) {
            throw new RuntimeException("Impossibile eliminare il file del database.");
        }
    }
}
