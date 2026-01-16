package br.com.ruasvivas.gameplay.listener;

import br.com.ruasvivas.common.model.RPGClass;
import br.com.ruasvivas.common.model.User;
import br.com.ruasvivas.gameplay.manager.CacheManager;
import br.com.ruasvivas.gameplay.manager.CooldownManager;
import br.com.ruasvivas.gameplay.manager.SkillManager;
import br.com.ruasvivas.api.skill.Skill;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkillListener implements Listener {

    private final CacheManager cacheManager;
    private final CooldownManager cooldownManager;
    private final SkillManager skillManager;

    // Anti-Spam Local
    private final Map<UUID, Long> spamProtection = new HashMap<>();

    public SkillListener(CacheManager cacheManager, CooldownManager cooldownManager, SkillManager skillManager) {
        this.cacheManager = cacheManager;
        this.cooldownManager = cooldownManager;
        this.skillManager = skillManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        User user = cacheManager.getUser(player);
        if (user == null || user.getRpgClass() == RPGClass.NOVICE) return; // Novatos não tem skill

        // Verifica Item na Mão (Conversão String -> Material)
        Material heldItem = player.getInventory().getItemInMainHand().getType();
        try {
            Material classIcon = Material.valueOf(user.getRpgClass().getIconMaterial());
            if (heldItem != classIcon) return;
        } catch (IllegalArgumentException e) {
            return;
        }

        event.setCancelled(true); // Impede defesa/arco vanilla

        Skill skill = skillManager.getSkill(user.getRpgClass());
        if (skill == null) return;

        // --- LÓGICA DE COOLDOWN E MANA ---

        if (cooldownManager.isOnCooldown(player.getUniqueId(), skill.getName())) {
            if (canSendWarning(player)) {
                double left = cooldownManager.getRemainingSeconds(player.getUniqueId(), skill.getName());
                Component msg = Component.text("Recarregando: " + String.format("%.1f", left) + "s").color(NamedTextColor.RED);
                cacheManager.sendWarning(player, msg);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            }
            return;
        }

        if (user.getMana() < skill.getManaCost()) {
            if (canSendWarning(player)) {
                cacheManager.sendWarning(player, Component.text("Mana insuficiente!").color(NamedTextColor.BLUE));
                player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 2f);
            }
            return;
        }

        boolean success = skill.cast(player);
        if (success) {
            user.setMana(user.getMana() - skill.getManaCost());
            cooldownManager.addCooldown(player.getUniqueId(), skill.getName(), skill.getCooldownSeconds());
            cacheManager.forceUpdate(player);
        }
    }

    private boolean canSendWarning(Player player) {
        long now = System.currentTimeMillis();
        long last = spamProtection.getOrDefault(player.getUniqueId(), 0L);
        if (now - last > 1000) {
            spamProtection.put(player.getUniqueId(), now);
            return true;
        }
        return false;
    }
}