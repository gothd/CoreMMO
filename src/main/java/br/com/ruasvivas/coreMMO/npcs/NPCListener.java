package br.com.ruasvivas.coreMMO.npcs;

import br.com.ruasvivas.coreMMO.CoreMMO;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class NPCListener implements Listener {

    private final CoreMMO plugin;

    public NPCListener(CoreMMO plugin) {
        this.plugin = plugin;
    }

    // INTERAÇÃO
    @EventHandler
    public void aoClicarNPC(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Entity clicado = event.getRightClicked();

        if (clicado.getScoreboardTags().contains("npc_interativo")) {
            event.setCancelled(true); // Evita abrir inventário do Villager

            String npcId = null;
            // Busca o ID na tag "npc:nome"
            for (String tag : clicado.getScoreboardTags()) {
                if (tag.startsWith("npc:")) {
                    npcId = tag.split(":")[1];
                    break;
                }
            }

            if (npcId != null) {
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);

                // Lógica simples de diálogo
                if (npcId.equals("ferreiro")) {
                    event.getPlayer().sendMessage(
                            Component.text("Borg: A forja está quente!").color(NamedTextColor.YELLOW)
                    );
                } else if (npcId.equals("guia")) {
                    event.getPlayer().sendMessage(
                            Component.text("Merlin: Bem-vindo a Seu Server.").color(NamedTextColor.AQUA)
                    );
                }
            }
        }
    }

    // IMORTALIDADE E LIMPEZA
    @EventHandler
    public void aoMachucarNPC(EntityDamageEvent evento) {
        if (evento.getEntity().getScoreboardTags().contains("npc_interativo")) {

            // Se for Dano de VOID (Comando /kill ou cair no void)
            if (evento.getCause() == EntityDamageEvent.DamageCause.VOID ||
                    evento.getCause() == EntityDamageEvent.DamageCause.SUICIDE) {

                evento.setCancelled(false); // Garante que o evento não está cancelado
                evento.getEntity().remove(); // Remove a entidade da existência
                return;
            }

            // Para todo o resto (espada, fogo, explosão), bloqueamos.
            evento.setCancelled(true);
        }
    }
}