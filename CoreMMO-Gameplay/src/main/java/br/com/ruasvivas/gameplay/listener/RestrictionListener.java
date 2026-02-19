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
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
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

    // Bloqueio de Inventário (Mouse, Shift, Teclas 1-9 e Tecla F)
    @EventHandler(priority = EventPriority.LOW)
    public void onEquip(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;


        ItemStack itemToEquip = null;

        // CASO A: Interação DIRETAMENTE no slot de armadura
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {

            // Segurando com o mouse e soltando no slot
            if (event.getCursor().getType() != Material.AIR) {
                itemToEquip = event.getCursor();
            }
            // Usando atalhos numéricos (1 a 9) da hotbar no slot de armadura
            else if (event.getClick() == ClickType.NUMBER_KEY && event.getHotbarButton() >= 0) {
                itemToEquip = player.getInventory().getItem(event.getHotbarButton());
            }
            // Usando a tecla 'F' (Swap da mão secundária) para forçar o item
            else if (event.getClick() == ClickType.SWAP_OFFHAND) {
                itemToEquip = player.getInventory().getItemInOffHand();
            }
        }

        // CASO B: Shift-Click do inventário de baixo para o slot de armadura
        else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {

            // Somente se o jogador está no próprio inventário (CRAFTING).
            // Se tiver num baú (CHEST), o shift-click manda pro baú, então permite.
            if (event.getView().getTopInventory().getType() == InventoryType.CRAFTING) {
                ItemStack clickedItem = event.getCurrentItem();

                if (clickedItem != null && InventoryUtil.isArmor(clickedItem.getType())) {
                    itemToEquip = clickedItem;
                }
            }
        }

        // Se o jogador está TENTANDO equipar algo, aplica a trava
        if (itemToEquip != null && itemToEquip.getType() != Material.AIR) {
            if (!checkRequirement(player, itemToEquip)) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                player.sendMessage(Component.text("Nível insuficiente para equipar!").color(NamedTextColor.RED));
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

    // Bloqueio Anti-Bug de Redstone (Tentativa de equipar usando Ejetores)
    @EventHandler(priority = EventPriority.LOW)
    public void onDispenserEquip(BlockDispenseArmorEvent event) {
        if (event.getTargetEntity() instanceof Player player) {
            if (!checkRequirement(player, event.getItem())) {
                event.setCancelled(true);
            }
        }
    }

    // Validador NBT
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