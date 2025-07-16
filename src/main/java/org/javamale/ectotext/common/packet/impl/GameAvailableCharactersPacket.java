package org.javamale.ectotext.common.packet.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.javamale.ectotext.common.packet.Packet;
import org.javamale.ectotext.common.packet.PacketType;

import java.io.IOException;
import java.util.*;

/**
 * Pacchetto con l’elenco dei personaggi ancora <em>selezionabili</em> in una
 * partita. Viene inviato dal server ai client quando avviene una richiesta
 * {@code GAME_GET_AVAILABLE_CHARACTERS} o quando lo stato cambia.
 * <p>
 * Contiene una lista di stringhe ({@link #characters}) che indicano i
 * personaggi non ancora prenotati o scelti da altri giocatori.
 * <p>
 * Esempio di JSON serializzato:
 * <pre>{
 *   "type": "game_available_characters",
 *   "characters": ["Egon", "Ray", "Winston"]
 * }</pre>
 */
public class GameAvailableCharactersPacket extends Packet {

    /** Lista dei nomi dei personaggi disponibili (ordine mantenuto). */
    private final List<String> characters;

    /* ------------------------------------------------------------------ */
    /*                             COSTRUTTORI                            */
    /* ------------------------------------------------------------------ */

    /** Costruttore vuoto richiesto da Gson/Reflection. */
    public GameAvailableCharactersPacket() {
        super(PacketType.GAME_AVAILABLE_CHARACTERS);
        characters = new ArrayList<>();
    }

    /**
     * Costruisce il pacchetto partendo da una collezione di nomi.
     *
     * @param characters collezione di personaggi disponibili
     */
    public GameAvailableCharactersPacket(Collection<String> characters) {
        this();
        this.characters.addAll(characters);
    }

    /** Variante var-args di {@link #GameAvailableCharactersPacket(Collection)}. */
    public GameAvailableCharactersPacket(String... characters) {
        this(Arrays.asList(characters));
    }

    /* ------------------------------------------------------------------ */
    /*                              GETTER                                */
    /* ------------------------------------------------------------------ */

    /**
     * Restituisce la lista dei personaggi disponibili (lista live e modificabile).
     *
     * @return lista di nomi
     */
    public List<String> getCharacters() {
        return characters;
    }

    /* ------------------------------------------------------------------ */
    /*                      METODI DI MUTAZIONE LISTA                     */
    /* ------------------------------------------------------------------ */

    /** Aggiunge un singolo personaggio alla lista. */
    public void addCharacter(String character) {
        characters.add(character);
    }

    /** Aggiunge una collezione di personaggi alla lista. */
    public void addCharacters(Collection<String> characters) {
        this.characters.addAll(characters);
    }

    /** Variante var-args di {@link #addCharacters(Collection)}. */
    public void addCharacters(String... characters) {
        Collections.addAll(this.characters, characters);
    }

    /* ------------------------------------------------------------------ */
    /*                       SERIALIZZAZIONE / GSON                       */
    /* ------------------------------------------------------------------ */

    /** {@inheritDoc} */
    @Override
    protected void toJson(JsonWriter writer) throws IOException {
        writer.name("characters");
        writer.beginArray();
        for (String character : characters) {
            writer.value(character);
        }
        writer.endArray();
    }

    /** {@inheritDoc} */
    @Override
    protected void fromJson(JsonReader reader) throws IOException {
        while (reader.hasNext()) {
            String name = reader.nextName();
            if ("characters".equals(name)) {
                reader.beginArray();
                while (reader.hasNext()) {
                    addCharacter(reader.nextString());
                }
                reader.endArray();
            } else {
                throw new IllegalStateException("Unexpected JSON field: " + name);
            }
        }
    }
}
