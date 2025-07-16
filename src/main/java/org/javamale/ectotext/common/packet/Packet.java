package org.javamale.ectotext.common.packet;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

/**
 * Classe base astratta per tutti i pacchetti di rete.
 * <p>
 * Ogni sottoclasse di {@code Packet} incapsula i dati necessari a un
 * particolare messaggio tra client e server.
 * La serializzazione/deserializzazione avviene in JSON tramite
 * <a href="https://github.com/google/gson">Gson</a>; il campo {@link #type}
 * (salvato come stringa <em>key</em>) viene scritto automaticamente in testa
 * al JSON mentre i campi specifici vengono gestiti dalle implementazioni
 * concrete mediante i metodi astratti {@link #toJson(JsonWriter)} e
 * {@link #fromJson(JsonReader)}.
 */
public abstract class Packet {

    /** Tipo del pacchetto, utile per routing e factory di parsing. */
    private final PacketType type;

    /**
     * Crea un nuovo pacchetto del tipo indicato.
     *
     * @param type enumerazione {@link PacketType} che identifica il messaggio
     */
    public Packet(PacketType type) {
        this.type = type;
    }

    /** @return il tipo di pacchetto associato all’istanza */
    public PacketType getType() {
        return type;
    }

    /* ====================================================================== */
    /*                Metodi che le sottoclassi DEVONO implementare           */
    /* ====================================================================== */

    /**
     * Scrive i campi specifici del pacchetto nel {@code JsonWriter}.
     * <p>
     * Il metodo viene invocato da {@link #toBaseJson()}; l’implementazione
     * concreta <strong>non</strong> deve creare né chiudere l’oggetto JSON, ma
     * solo aggiungere propri attributi (e.g. {@code writer.name("x").value(42);}).
     *
     * @param writer writer già posizionato all’interno di un oggetto JSON
     * @throws IOException errori di I/O in scrittura
     */
    protected abstract void toJson(JsonWriter writer) throws IOException;

    /**
     * Legge i campi specifici del pacchetto dal {@code JsonReader}.
     * <p>
     * Il lettore è posizionato subito dopo il campo {@code type}; l’implementazione
     * concreta deve leggere tutti gli altri attributi e terminare lasciando
     * al chiamante la chiusura dell’oggetto.
     *
     * @param reader lettore già posizionato sull’inizio dell’oggetto JSON
     * @throws IOException errori di I/O in lettura
     */
    protected abstract void fromJson(JsonReader reader) throws IOException;

    /* ====================================================================== */
    /*                        SERIALIZZAZIONE DI BASE                         */
    /* ====================================================================== */

    /**
     * Serializza il pacchetto in una stringa JSON.
     * <p>
     * La struttura generata è del tipo:
     * <pre>{
     *   "type": "chiave_pacchetto",
     *   ...altri campi...
     * }</pre>
     *
     * @return rappresentazione JSON del pacchetto
     * @throws RuntimeException se si verifica un errore di I/O durante la scrittura
     */
    public String toBaseJson() {
        Gson gson = new Gson();
        StringWriter writer = new StringWriter();

        try {
            JsonWriter jsonWriter = gson.newJsonWriter(writer);
            // jsonWriter.setIndent("  "); // opzionale

            jsonWriter.beginObject();
            jsonWriter.name("type").value(type.getKey());

            // delega ai campi specifici della sottoclasse
            toJson(jsonWriter);

            jsonWriter.endObject();
            jsonWriter.close();
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* ====================================================================== */
    /*                       DESERIALIZZAZIONE DI BASE                        */
    /* ====================================================================== */

    /**
     * Deserializza un pacchetto a partire dalla stringa JSON.
     * <p>
     * Il metodo:
     * <ol>
     *   <li>legge il campo {@code type};</li>
     *   <li>risolve il {@link PacketType} via {@link PacketType#fromKey(String)};</li>
     *   <li>crea un’istanza vuota tramite reflection
     *       ({@link PacketType#getPacket()});</li>
     *   <li>deleg
     *       a la lettura dei campi specifici a {@link #fromJson(JsonReader)}.</li>
     * </ol>
     *
     * @param json stringa JSON proveniente dal socket o da file
     * @return pacchetto ricostruito, oppure {@code null} in caso di errore
     */
    public static Packet fromBaseJson(String json) {
        Gson gson = new Gson();

        StringReader reader = new StringReader(json);
        JsonReader jsonReader = gson.newJsonReader(reader);

        try {
            jsonReader.beginObject();

            String typeName = jsonReader.nextName();
            if (typeName.equalsIgnoreCase("type")) {

                PacketType type = PacketType.fromKey(jsonReader.nextString());
                if (type == null)
                    throw new IllegalArgumentException("Unknown packet type");

                Packet packet = type.getPacket();
                packet.fromJson(jsonReader);

                jsonReader.endObject();
                jsonReader.close();
                return packet;
            } else {
                throw new IllegalArgumentException("Invalid packet format");
            }
        } catch (IOException |
                 InvocationTargetException |
                 NoSuchMethodException |
                 InstantiationException |
                 IllegalAccessException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
}
