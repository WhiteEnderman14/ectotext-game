package org.javamale.ectotext.server.rest;

import jakarta.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.javamale.ectotext.server.rest.service.GameRoomService;
import org.javamale.ectotext.server.rest.service.HealthService;
import org.javamale.ectotext.server.rest.service.SocketService;

import java.io.IOException;
import java.net.URI;

/**
 * Server HTTP RESTful per l'esposizione dei servizi di gioco.
 * Questa classe gestisce un server HTTP basato su Jersey/Grizzly che espone:
 * <ul>
 *   <li>Endpoint di health check per monitoraggio</li>
 *   <li>Servizi per la gestione delle stanze di gioco</li>
 *   <li>Informazioni sullo stato del server socket</li>
 * </ul>
 * Il server viene configurato per accettare connessioni su tutte le interfacce di rete (0.0.0.0)
 * e utilizza Jersey per la gestione delle richieste REST.
 *
 * @see HealthService
 * @see SocketService
 * @see GameRoomService
 */
public class RestServer {
    /** Porta su cui il server REST è in ascolto. */
    private final int PORT;
    
    /** Istanza del server HTTP Grizzly. */
    private final HttpServer httpServer;

    /**
     * Crea e configura una nuova istanza del server REST.
     * Il costruttore:
     * <ol>
     *   <li>Configura l'URI base del server (http://0.0.0.0:port/)</li>
     *   <li>Registra i servizi REST disponibili</li>
     *   <li>Inizializza il server HTTP Grizzly</li>
     * </ol>
     *
     * @param port porta su cui il server deve essere in ascolto
     * @see GrizzlyHttpServerFactory
     */
    public RestServer(int port) {
        this.PORT = port;
        URI baseUri = UriBuilder.fromUri("http://0.0.0.0/").port(PORT).build();
        ResourceConfig config = new ResourceConfig(HealthService.class, SocketService.class, GameRoomService.class);

        httpServer = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);
    }

    /**
     * Restituisce la porta configurata per il server.
     *
     * @return numero della porta su cui il server è in ascolto
     */
    public int getPort() {
        return PORT;
    }

    /**
     * Avvia il server REST.
     * <p>
     * Una volta avviato, il server inizierà ad accettare connessioni HTTP
     * sulla porta configurata e gestirà le richieste REST attraverso i
     * servizi registrati.
     * </p>
     *
     * @throws IOException se si verificano errori durante l'avvio del server,
     *                     ad esempio se la porta è già in uso
     */
    public void start() throws IOException {
        httpServer.start();
    }

    /**
     * Arresta il server REST in modo ordinato.
     * L'arresto:
     * <ul>
     *   <li>Completa le richieste in corso</li>
     *   <li>Chiude tutte le connessioni attive</li>
     *   <li>Libera le risorse allocate</li>
     * </ul>
     */
    public void shutdown() {
        httpServer.shutdown();
    }
}