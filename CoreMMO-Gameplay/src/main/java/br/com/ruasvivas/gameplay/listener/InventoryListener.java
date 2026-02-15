package br.com.ruasvivas.gameplay.listener;

import br.com.ruasvivas.gameplay.CorePlugin;
import br.com.ruasvivas.gameplay.manager.CacheManager;
import br.com.ruasvivas.gameplay.util.StatHelper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    private final CorePlugin plugin;
    private final CacheManager cacheManager;

    public InventoryListener(CorePlugin plugin, CacheManager cacheManager) {
        this.plugin = plugin;
        this.cacheManager = cacheManager;
    }

    // Evento genérico para recalcular stats ao fechar o inventário
    // É a forma mais segura e leve de detectar mudanças de armadura
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            // Verificação básica se é um usuário carregado
            if (cacheManager.getUser(player) != null) {
                StatHelper.updateVisualArmor(player);
            }
        }
    }

    // Detecta clique direito para equipar armadura (Hotbar)
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.hasItem()) return;
        ItemStack item = event.getItem();
        if (item == null) return;

        String type = item.getType().name();
        if (type.endsWith("_HELMET") || type.endsWith("_CHESTPLATE") ||
                type.endsWith("_LEGGINGS") || type.endsWith("_BOOTS")) {

            // Atrasa 1 tick para o item realmente ir para o corpo antes de calcular
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                StatHelper.updateVisualArmor(event.getPlayer());
            }, 1L);
        }
    }
}