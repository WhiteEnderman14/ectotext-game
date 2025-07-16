package org.javamale.ectotext.server.rest.service;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Servizio REST per il monitoraggio dello stato del server.
 * Questo servizio implementa un endpoint di health check che:
 * <ul>
 *   <li>Verifica lo stato di attività del server</li>
 *   <li>Fornisce informazioni sullo stato del sistema</li>
 *   <li>Può essere utilizzato per il monitoring automatizzato</li>
 * </ul>
 * 
 * <p>
 * Tutti gli endpoint sono accessibili sotto il path base {@code /api}.
 * Le risposte sono sempre in formato JSON per garantire
 * compatibilità con sistemi di monitoraggio standard.
 * </p>
 */
@Path("/api")
public class HealthService {

    /**
     * Verifica lo stato di salute del server.
     * Questo endpoint REST:
     * <ul>
     *   <li>Metodo: GET</li>
     *   <li>Path: /api/health</li>
     *   <li>Produce: application/json</li>
     * </ul>
     *
     * Il formato della risposta JSON è:
     * <pre>
     * {
     *   "status": "UP"
     * }
     * </pre>
     * dove:
     * <ul>
     *   <li>"UP" indica che il server è attivo e funzionante</li>
     * </ul>
     *
     * @return Response con status 200 (OK) e stato del server in formato JSON
     * @see Response#ok(Object)
     */
    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public Response healthCheck() {
        return Response.ok("{\"status\":\"UP\"}", MediaType.APPLICATION_JSON).build();
    }
}