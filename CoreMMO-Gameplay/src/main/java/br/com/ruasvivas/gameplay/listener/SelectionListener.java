package br.com.ruasvivas.gameplay.listener;

import br.com.ruasvivas.gameplay.manager.SelectionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SelectionListener implements Listener {

    private final SelectionManager selectionManager;
    private static final Material WAND = Material.GOLDEN_AXE;

    public SelectionListener(SelectionManager selectionManager) {
        this.selectionManager = selectionManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("coremmo.admin")) return;
        if (event.getItem() == null || event.getItem().getType() != WAND) return;
        if (event.getClickedBlock() == null) return;

        // Cancela para não quebrar o bloco em que bateu o machado
        event.setCancelled(true);

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            selectionManager.setPos1(player, event.getClickedBlock().getLocation());
            player.sendMessage(Component.text("Posição 1 fixada!").color(NamedTextColor.LIGHT_PURPLE));

        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            selectionManager.setPos2(player, event.getClickedBlock().getLocation());
            player.sendMessage(Component.text("Posição 2 fixada!").color(NamedTextColor.LIGHT_PURPLE));
        }
    }

    // Auto-Limpeza se desconectar-se no meio da seleção
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        selectionManager.clearSelection(event.getPlayer().getUniqueId());
    }
}