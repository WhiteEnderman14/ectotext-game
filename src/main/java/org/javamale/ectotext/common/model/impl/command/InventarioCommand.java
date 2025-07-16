package org.javamale.ectotext.common.model.impl.command;

import org.javamale.ectotext.common.model.*;
import org.javamale.ectotext.common.model.Character;
import org.javamale.ectotext.common.packet.impl.GameNarratorPacket;

import java.util.List;

/**
 * Classe InventarioCommand.
 * <p>
 * Responsabilità principale di InventarioCommand: gestisce il comando che permette di visualizzare
 * il contenuto dell’inventario del personaggio, restituendo una descrizione testuale degli oggetti posseduti.
 * </p>
 */
public class InventarioCommand implements CommandHandler {

    /**
     * Esegue il comando inventario.
     * <p>
     * Restituisce una risposta narrativa che elenca gli oggetti presenti nell’inventario del personaggio.
     * Se l’inventario è vuoto, comunica all’utente che non possiede oggetti.
     * </p>
     *
     * @param character       personaggio che invoca il comando
     * @param gameDescription descrizione globale del gioco
     * @param gameState       stato corrente del gioco
     * @param args            argomenti passati dal giocatore (ignorati in questo comando)
     * @return lista di risposte {@link CommandResponse} contenente un solo messaggio narrativo
     */
    @Override
    public List<CommandResponse> execute(Character character, GameDescription gameDescription, GameState gameState, String[] args) {
        if (character.getInventory().isEmpty()){
            return List.of(new CommandResponse(new GameNarratorPacket("Il tuo inventario è vuoto."), false));
        }

        StringBuilder inv = new StringBuilder();

        inv.append("Il tuo inventario: ");

        character.getInventory().forEach(item -> {
            inv.append("\n   . ").append(item.getDisplayName()).append("\n    - ").append(item.getDescription());
        });

        return List.of(new CommandResponse(new GameNarratorPacket(inv.toString()), false));
    }
}
