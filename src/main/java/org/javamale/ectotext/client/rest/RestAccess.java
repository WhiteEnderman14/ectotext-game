package org.javamale.ectotext.client.rest;

import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.impl.RoomDetailsPacket;
import org.javamale.ectotext.common.packet.impl.RoomListPacket;

import java.io.StringReader;
import java.util.Collection;

/**
 * Fornisce metodi statici per l'accesso ai servizi REST del server di gioco.
 * Questa classe gestisce le richieste HTTP per:
 * <ul>
 *   <li>Verifica dello stato del server</li>
 *   <li>Recupero delle informazioni sulle stanze</li>
 *   <li>Recupero delle informazioni sugli utenti</li>
 *   <li>Configurazione della connessione socket</li>
 * </ul>
 * <p>
 * Tutte le chiamate sono gestite in modo sicuro, con gestione delle eccezioni
 * e chiusura appropriata delle risorse.
 * </p>
 */
public class RestAccess {

    /**
     * Verifica se il server REST è raggiungibile e funzionante.
     * <p>
     * Effettua una richiesta GET all'endpoint /api/health e verifica
     * che la risposta sia HTTP 200 OK.
     * </p>
     *
     * @param host indirizzo del server (può essere IP o hostname)
     * @param port porta su cui il servizio REST è in ascolto
     * @return true se il server risponde correttamente, false in caso di errori
     *         o timeout
     */
    public static boolean isHealthy(String host, int port) {
        boolean healthy = false;

        try {
            Client webClient = ClientBuilder.newClient();
            WebTarget target = webClient.target("http://" + host + ":" + port + "/api/health");
            Response response = target.request().get();

            healthy = response.getStatus() == Response.Status.OK.getStatusCode();

            response.close();
            webClient.close();

        } catch (Exception ignored) {
        }

        return healthy;
    }

    /**
     * Recupera la lista delle stanze di gioco disponibili.
     * <p>
     * Effettua una richiesta GET all'endpoint /api/rooms e converte
     * la risposta JSON in oggetti {@link RoomListPacket.RoomListEntry}.
     * </p>
     *
     * @param host indirizzo del server (può essere IP o hostname)
     * @param port porta su cui il servizio REST è in ascolto
     * @return collezione di stanze disponibili, o null in caso di errore
     * @see RoomListPacket
     */
    public static Collection<RoomListPacket.RoomListEntry> retrieveRoomList(String host, int port) {
        Collection<RoomListPacket.RoomListEntry> rooms = null;

        try {
            Client webClient = ClientBuilder.newClient();
            WebTarget target = webClient.target("http://" + host + ":" + port + "/api/rooms");
            Response response = target.request(MediaType.APPLICATION_JSON).get();

            String responseString = response.readEntity(String.class);

            RoomListPacket packet = (RoomListPacket) Packet.fromBaseJson(responseString);
            rooms = packet.getRoomList();

            response.close();
            webClient.close();

        } catch (Exception ignored) {
        }

        return rooms;
    }

    /**
     * Recupera la lista degli utenti presenti in una specifica stanza.
     * <p>
     * Effettua una richiesta GET all'endpoint /api/rooms/{roomName} e converte
     * la risposta JSON in una lista di nomi utente.
     * </p>
     *
     * @param host indirizzo del server (può essere IP o hostname)
     * @param port porta su cui il servizio REST è in ascolto
     * @param roomName nome della stanza di cui recuperare gli utenti
     * @return collezione di nomi degli utenti nella stanza, o null in caso di errore
     * @see RoomDetailsPacket
     */
    public static Collection<String> retriveRoomUsers(String host, int port, String roomName) {
        Collection<String> users = null;

        try {
            Client webClient = ClientBuilder.newClient();
            WebTarget target = webClient.target("http://" + host + ":" + port + "/api/rooms/" + roomName);
            Response response = target.request(MediaType.APPLICATION_JSON).get();

            String responseString = response.readEntity(String.class);

            RoomDetailsPacket packet = (RoomDetailsPacket) Packet.fromBaseJson(responseString);
            users = packet.getUsers();

            response.close();
            webClient.close();

        } catch (Exception ignored) {
        }

        return users;
    }

    /**
     * Recupera la porta del server socket utilizzata per la comunicazione in tempo reale.
     * <p>
     * Effettua una richiesta GET all'endpoint /api/socket/port e analizza
     * la risposta JSON per estrarre il numero di porta.
     * </p>
     *
     * @param host indirizzo del server (può essere IP o hostname)
     * @param port porta su cui il servizio REST è in ascolto
     * @return numero della porta socket, o -1 in caso di errore
     * @throws JsonParseException se la risposta non contiene un formato valido per la porta
     */
    public static int retrieveSocketPort(String host, int port) {
        int socketPort = -1;

        try {
            Client webClient = ClientBuilder.newClient();
            WebTarget target = webClient.target("http://" + host + ":" + port + "/api/socket/port");
            Response response = target.request(MediaType.APPLICATION_JSON).get();

            String responseString = response.readEntity(String.class);

            try(JsonReader jsonReader = new JsonReader(new StringReader(responseString))) {
                jsonReader.beginObject();
                if (jsonReader.hasNext() && jsonReader.nextName().equals("port")) {
                    socketPort = jsonReader.nextInt();
                } else {
                    throw new JsonParseException("Invalid port format");
                }

                jsonReader.endObject();
            }

            response.close();
            webClient.close();

        } catch (Exception ignored) {
        }

        return socketPort;
    }
}