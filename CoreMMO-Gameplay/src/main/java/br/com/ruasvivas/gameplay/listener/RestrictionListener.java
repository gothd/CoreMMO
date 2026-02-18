package br.com.ruasvivas.gameplay.listener;

import br.com.ruasvivas.common.model.User;
import br.com.ruasvivas.gameplay.manager.CacheManager;
import br.com.ruasvivas.gameplay.util.BukkitConstants;
import br.com.ruasvivas.gameplay.util.InventoryUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class RestrictionListener implements Listener {

    private final CacheManager cacheManager;

    public RestrictionListener(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // Bloqueio de Ataque (Armas)
    @EventHandler(priority = EventPriority.LOW)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) return;
        if (!checkRequirement(player, item)) {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            player.sendMessage(Component.text("Você não tem nível para usar esta arma!").color(NamedTextColor.RED));
        }
    }

    // Bloqueio de Equipar (Clique no Slot de Armadura)
    @EventHandler(priority = EventPriority.LOW)
    public void onEquip(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getSlotType() != InventoryType.SlotType.ARMOR) return;

        // CASO A: Clique direto no slot de armadura (Equipar/Trocar)
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            // Aqui o "Cursor" é o item que está sendo colocado no slot
            ItemStack cursorItem = event.getCursor();
            if (cursorItem.getType() != Material.AIR) {
                if (!checkRequirement(player, cursorItem)) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    player.sendMessage(Component.text("Nível insuficiente para equipar!").color(NamedTextColor.RED));
                }
            }
            return;
        }
        // CASO B: Shift-Click (Do inventário para o corpo)
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        if (event.isShiftClick()) {
            // Verifica se é um item de armadura ANTES de ler NBT
            if (!InventoryUtil.isArmor(item.getType())) return;

            // Verifica se o slot de destino está vazio (Lógica nativa do Minecraft)
            // Se o slot já estiver ocupado, o Shift-Click não equipa, apenas move, então não precisa bloquear.
            if (InventoryUtil.isSlotEmptyFor(player.getInventory(), item.getType())) {
                if (!checkRequirement(player, item)) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    player.sendMessage(Component.text("Nível insuficiente para equipar!").color(NamedTextColor.RED));
                }
            }
        }
    }

    // Bloqueio de Equipar Rápido (Clique Direito na Hotbar)
    @EventHandler(priority = EventPriority.LOW)
    public void onQuickEquip(PlayerInteractEvent event) {
        if (!event.hasItem()) return;
        ItemStack item = event.getItem();
        if (item == null) return;

        if (InventoryUtil.isArmor(item.getType())) {
            if (!checkRequirement(event.getPlayer(), item)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Component.text("Nível insuficiente!").color(NamedTextColor.RED));
            }
        }
    }

    private boolean checkRequirement(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return true;

        ItemMeta meta = item.getItemMeta();
        Integer reqLevel = meta.getPersistentDataContainer().get(BukkitConstants.RPG_REQ_LEVEL_KEY, PersistentDataType.INTEGER);

        if (reqLevel == null) return true; // Sem requisito

        User user = cacheManager.getUser(player);
        if (user == null) return true;

        return user.getLevel() >= reqLevel;
    }
}