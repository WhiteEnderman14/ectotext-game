package org.javamale.ectotext.server.rest.service;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.javamale.ectotext.common.packet.ErrorCode;
import org.javamale.ectotext.common.packet.impl.CreateRoomPacket;
import org.javamale.ectotext.common.packet.impl.ErrorPacket;
import org.javamale.ectotext.server.GameServer;
import org.javamale.ectotext.server.core.GameRoom;
import org.javamale.ectotext.server.util.PacketFactory;

import java.util.Collection;

/**
 * Servizio REST per la gestione delle stanze di gioco.
 * Questo servizio espone API RESTful per:
 * <ul>
 *   <li>Visualizzare le stanze disponibili</li>
 *   <li>Ottenere dettagli di stanze specifiche</li>
 *   <li>Creare nuove stanze di gioco</li>
 * </ul>
 *
 * Tutte le risposte sono in formato JSON e includono:
 * <ul>
 *   <li>Codici di stato HTTP appropriati</li>
 *   <li>Headers Content-Type corretti</li>
 *   <li>Payload strutturati secondo il protocollo</li>
 * </ul>
 *
 * @see GameRoom
 * @see GameServer
 * @see ErrorPacket
 */

@Path("/api/")
public class GameRoomService {

    /**
     * Recupera l'elenco di tutte le stanze attive.
     * <p>
     * Endpoint: GET /api/rooms
     * </p>
     *
     * La risposta include per ogni stanza:
     * <ul>
     *   <li>Nome identificativo</li>
     *   <li>Numero di giocatori connessi</li>
     *   <li>Stato corrente</li>
     * </ul>
     *
     * @return risposta HTTP 200 OK con JSON contenente la lista delle stanze
     * @see Response
     * @see PacketFactory#fromGameRoomList
     */
    @GET
    @Path("/rooms")
    @Produces("application/json")
    public Response getRoomList() {
        Collection<GameRoom> rooms = GameServer.getRoomManager().getGameRooms();
        String json = PacketFactory.fromGameRoomList(rooms).toBaseJson();

        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    /**
     * Recupera i dettagli di una specifica stanza.
     * <p>
     * Endpoint: GET /api/rooms/{room_name}
     * </p>
     *
     * Possibili risposte:
     * <ul>
     *   <li>200 OK - Stanza trovata, dettagli nel body</li>
     *   <li>404 Not Found - Stanza non esistente</li>
     * </ul>
     *
     * @param roomName identificatore univoco della stanza
     * @return risposta HTTP con dettagli della stanza o errore
     * @see Response
     * @see PacketFactory#fromGameRoom
     * @see ErrorCode#ROOM_NOT_FOUND
     */
    @GET
    @Path("/rooms/{room_name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoomByName(@PathParam("room_name") String roomName) {
        GameRoom room = GameServer.getRoomManager().getGameRoom(roomName);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorPacket(ErrorCode.ROOM_NOT_FOUND).toBaseJson())
                    .build();
        }

        String json = PacketFactory.fromGameRoom(room).toBaseJson();
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    /**
     * Crea una nuova stanza di gioco.
     * <p>
     * Endpoint: POST /api/rooms
     * </p>
     *
     * Il body della richiesta deve contenere un JSON con:
     * <ul>
     *   <li>roomName: nome univoco della stanza (required)</li>
     *   <li>roomPassword: password di accesso (optional)</li>
     * </ul>
     *
     * Possibili risposte:
     * <ul>
     *   <li>201 Created - Stanza creata con successo</li>
     *   <li>400 Bad Request - Formato richiesta non valido</li>
     *   <li>409 Conflict - Nome stanza già in uso</li>
     * </ul>
     *
     * @param body JSON contenente i parametri della stanza
     * @return risposta HTTP con esito dell'operazione
     * @see Response
     * @see CreateRoomPacket
     * @see ErrorCode#INVALID_PACKET
     * @see ErrorCode#ROOM_ALREADY_EXISTS
     */
    @POST
    @Path("/rooms")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(String body) {
        CreateRoomPacket packet;
        try {
            packet = (CreateRoomPacket) ErrorPacket.fromBaseJson(body);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorPacket(ErrorCode.INVALID_PACKET).toBaseJson())
                    .build();
        }

        String roomName = packet.getRoomName();
        String roomPassword = packet.getRoomPassword();

        if (roomName == null || roomName.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorPacket(ErrorCode.INVALID_PACKET).toBaseJson())
                    .build();
        }

        if (!GameServer.getRoomManager().createGameRoom(roomName, roomPassword)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorPacket(ErrorCode.ROOM_ALREADY_EXISTS).toBaseJson())
                    .build();
        }

        return Response.status(Response.Status.CREATED).build();
    }
}
