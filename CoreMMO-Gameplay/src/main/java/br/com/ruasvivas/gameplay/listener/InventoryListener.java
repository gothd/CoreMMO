package br.com.ruasvivas.gameplay.listener;

import br.com.ruasvivas.gameplay.CorePlugin;
import br.com.ruasvivas.gameplay.manager.CacheManager;
import br.com.ruasvivas.gameplay.util.InventoryUtil;
import br.com.ruasvivas.gameplay.util.StatHelper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    private final CorePlugin plugin;
    private final CacheManager cacheManager;

    public InventoryListener(CorePlugin plugin, CacheManager cacheManager) {
        this.plugin = plugin;
        this.cacheManager = cacheManager;
    }

    // Fecha o inventário -> Recalcula
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            if (cacheManager.getUser(player) != null) {
                // Pequeno delay para garantir que o servidor processou a movimentação do item
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    StatHelper.updateVisualArmor(player);
                }, 1L);
            }
        }
    }

    // Clique no slot -> Recalcula
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Se for Slot de Armadura OU (Shift-Click em item de Armadura E Slot vazio)
        boolean shouldUpdate = false;

        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            shouldUpdate = true;
        } else if (event.isShiftClick() && event.getCurrentItem() != null) {
            Material mat = event.getCurrentItem().getType();
            // Só atualiza se for armadura e o slot estiver vazio (ou seja, vai equipar)
            if (InventoryUtil.isArmor(mat) && InventoryUtil.isSlotEmptyFor(player.getInventory(), mat)) {
                shouldUpdate = true;
            }
        }

        if (shouldUpdate) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                StatHelper.updateVisualArmor(player);
            }, 1L);
        }
    }

    // Clique Direito (Equipar rápido) -> Recalcula
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.hasItem()) return;
        ItemStack item = event.getItem();
        if (item == null) return;

        String type = item.getType().name();
        if (type.endsWith("_HELMET") || type.endsWith("_CHESTPLATE") ||
                type.endsWith("_LEGGINGS") || type.endsWith("_BOOTS")) {

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                StatHelper.updateVisualArmor(event.getPlayer());
            }, 1L);
        }
    }
}