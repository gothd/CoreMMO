package br.com.ruasvivas.gameplay.listener;

import br.com.ruasvivas.api.CoreRegistry;
import br.com.ruasvivas.gameplay.manager.NPCManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;

public class NPCListener implements Listener {

    // Serializador para transformar "&eTexto" em cor real
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        // Filtra para mão principal apenas (evita disparar 2x)
        if (event.getHand() != EquipmentSlot.HAND) return;

        Entity entity = event.getRightClicked();

        if (!entity.getScoreboardTags().contains(NPCManager.NPC_TAG)) return;

        event.setCancelled(true); // Bloqueia troca de Villager ou inventário de mob
        Player player = event.getPlayer();

        // Extrai o ID da tag "npc:id"
        String npcId = null;
        for (String tag : entity.getScoreboardTags()) {
            if (tag.startsWith("npc:")) {
                npcId = tag.split(":")[1]; //
                break;
            }
        }

        if (npcId != null) {
            handleDialogue(player, npcId);
        }
    }

    private void handleDialogue(Player player, String npcId) {
        // Pega o Manager do Registry
        NPCManager manager = CoreRegistry.get(NPCManager.class);

        // Busca falas do Config (Dinâmico)
        List<String> lines = manager.getDialogue(npcId);

        if (lines.isEmpty()) {
            // Fallback se esquecer de configurar no YAML
            player.sendMessage(serializer.deserialize("&7[...]"));
            return;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f); //

        // Envia todas as linhas configuradas
        for (String line : lines) {
            player.sendMessage(serializer.deserialize(line));
        }

        // TODO: Futuro V1.1: Aqui entra a lógica de Quests.
        // if (questManager.hasQuest(player, npcId)) ...
    }

    // --- PROTEÇÃO E LIMPEZA ---

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity().getScoreboardTags().contains(NPCManager.NPC_TAG)) {

            // Permite remoção via comando /kill ou cair no void (Admin Tools)
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID ||
                    event.getCause() == EntityDamageEvent.DamageCause.SUICIDE) {
                event.setCancelled(false);
                event.getEntity().remove(); // Limpa a entidade
                return;
            }
            // Bloqueia qualquer outro dano (Espada, Fogo, Explosão)
            event.setCancelled(true);
        }
    }
}