package org.javamale.ectotext.server.rest.service;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.javamale.ectotext.server.GameServer;

/**
 * Servizio REST per la gestione delle informazioni del server socket.
 * Questo servizio espone endpoint REST che forniscono:
 * <ul>
 *   <li>Informazioni sulla configurazione del server socket</li>
 *   <li>Dettagli di connessione necessari ai client</li>
 * </ul>
 * Tutti gli endpoint sono accessibili sotto il path base {@code /api/socket}.
 *
 * <p>
 * Le risposte sono sempre in formato JSON per garantire
 * compatibilità con client di diverso tipo.
 * </p>
 */
@Path("/api/socket")
public class SocketService {

    /**
     * Recupera la porta del server socket.
     * Questo endpoint REST:
     * <ul>
     *   <li>Metodo: GET</li>
     *   <li>Path: /api/socket/port</li>
     *   <li>Produce: application/json</li>
     * </ul>
     *
     * Il formato della risposta JSON è:
     * <pre>
     * {
     *   "port": numero_porta
     * }
     * </pre>
     *
     * @return Response con status 200 (OK) e la porta del socket in formato JSON
     * @see GameServer#getSocketPort()
     * @see MediaType#APPLICATION_JSON
     */
    @GET
    @Path("/port")
    @Produces("application/json")
    public Response getPort() {
        return Response.ok("{\"port\":" + GameServer.getSocketPort() + "}", MediaType.APPLICATION_JSON).build();
    }
}