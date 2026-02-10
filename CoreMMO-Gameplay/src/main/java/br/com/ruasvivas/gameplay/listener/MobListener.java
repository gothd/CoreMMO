package br.com.ruasvivas.gameplay.listener;

import br.com.ruasvivas.common.model.User;
import br.com.ruasvivas.gameplay.manager.CacheManager;
import br.com.ruasvivas.gameplay.manager.ItemGenerator;
import br.com.ruasvivas.gameplay.manager.LootManager;
import br.com.ruasvivas.gameplay.manager.MobManager;
import br.com.ruasvivas.gameplay.ui.ScoreboardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MobListener implements Listener {

    private final MobManager mobManager;
    private final CacheManager cacheManager;
    private final ScoreboardManager scoreboardManager;
    private final LootManager lootManager;
    private final Random random = new Random();

    public MobListener(MobManager mobManager, CacheManager cacheManager, ScoreboardManager scoreboardManager, LootManager lootManager) {
        this.mobManager = mobManager;
        this.cacheManager = cacheManager;
        this.scoreboardManager = scoreboardManager;
        this.lootManager = lootManager;
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof LivingEntity living) {
            // Só configura se ainda NÃO tiver nível (evita re-configurar em chunk load)
            if (!living.getPersistentDataContainer().has(mobManager.LEVEL_KEY, org.bukkit.persistence.PersistentDataType.INTEGER)) {
                mobManager.setupMob(living);
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        // Remove o nome do mob no momento da morte.
        // Assim o servidor acha que é um mob comum e não manda broadcast/log.
        event.getEntity().customName(null);
        event.getEntity().setCustomNameVisible(false);

        event.getDrops().clear(); // Limpa drops vanilla imediatamente

        LivingEntity mob = event.getEntity();
        Player killer = mob.getKiller();

        if (killer == null) return;

        int mobLevel = mobManager.getMobLevel(mob);
        User user = cacheManager.getUser(killer);

        long xpGain = 0;
        long coinsGain = 0;

        if (user != null) {
            // Cálculo de Recompensas (XP e Ouro)
            xpGain = 10 + (mobLevel * 5L);
            coinsGain = random.nextInt(5) + mobLevel;

            user.setExperience(user.getExperience() + xpGain);
            user.setCoins(user.getCoins() + coinsGain);

            // Toca som se for nível alto
            if (mobLevel >= 5) {
                killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1f);
            }

            // Atualiza o Scoreboard lateral (Ouro/Nível)
            scoreboardManager.updateScoreboard(killer);
        }

        // Sistema de Loot (Passa o XP ganho para exibir junto caso drop algo)
        handleCustomLoot(event, mob.getType(), mobLevel, killer, xpGain, coinsGain);
    }

    private void handleCustomLoot(EntityDeathEvent event, EntityType type, int level, Player killer, long xpGain, long coinsGain) {
        // OBTÉM A LISTA DE DROPS DO CONFIG
        List<ItemStack> generatedDrops = lootManager.getDropsFor(type, level);

        if (generatedDrops.isEmpty()) {
            // Se não dropou nada, manda só a mensagem de XP
            Component msg = Component.text("+ " + xpGain + " XP  |  + " + coinsGain + " Ouro", NamedTextColor.GOLD);
            cacheManager.sendWarning(killer, msg);
            return;
        }

        // PROCESSA OS ITENS (Auto-Loot)
        StringBuilder itemNames = new StringBuilder();
        boolean bagFull = false;
        boolean rareDrop = false;

        for (ItemStack item : generatedDrops) {
            // Verifica se é item RPG (tem nome customizado) para efeito visual
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                rareDrop = true;
            }

            HashMap<Integer, ItemStack> sobras = killer.getInventory().addItem(item);

            if (!sobras.isEmpty()) {
                bagFull = true;
                for (ItemStack sobra : sobras.values()) {
                    killer.getWorld().dropItemNaturally(killer.getLocation(), sobra);
                }
            }

            // Adiciona nome na lista para mensagem
            if (!itemNames.isEmpty()) itemNames.append(", ");
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                itemNames.append(Component.text(item.getItemMeta().getDisplayName()).content()); // Simplificação
            } else {
                itemNames.append(item.getType().name()); // Nome padrão
            }
        }

        // FEEDBACK VISUAL
        if (bagFull) {
            killer.sendMessage(Component.text("Mochila cheia! Alguns itens caíram no chão.").color(NamedTextColor.RED));
        } else {
            killer.playSound(killer.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1f);
        }

        if (rareDrop) {
            event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1f);
        }

        // Mensagem Combo: Itens + XP
        Component message = Component.text("Obteve: ", NamedTextColor.GREEN)
                .append(Component.text(itemNames.toString(), NamedTextColor.AQUA))
                .append(Component.text(" (+ " + xpGain + " XP)", NamedTextColor.GRAY));

        //  Prioridade Alta
        cacheManager.sendWarning(killer, message);
    }

    // Auxiliares para organizar
    private boolean isHumanoid(EntityType type) {
        return type == EntityType.ZOMBIE || type == EntityType.SKELETON ||
                type == EntityType.PIGLIN || type == EntityType.WITHER_SKELETON;
    }
}