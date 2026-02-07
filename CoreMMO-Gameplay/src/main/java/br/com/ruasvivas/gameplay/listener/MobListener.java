package br.com.ruasvivas.gameplay.listener;

import br.com.ruasvivas.common.model.User;
import br.com.ruasvivas.gameplay.manager.CacheManager;
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
import java.util.Random;

public class MobListener implements Listener {

    private final MobManager mobManager;
    private final CacheManager cacheManager;
    private final ScoreboardManager scoreboardManager;
    private final Random random = new Random();

    public MobListener(MobManager mobManager, CacheManager cacheManager, ScoreboardManager scoreboardManager) {
        this.mobManager = mobManager;
        this.cacheManager = cacheManager;
        this.scoreboardManager = scoreboardManager;
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
        LivingEntity mob = event.getEntity();
        Player killer = mob.getKiller();

        if (killer == null) return;

        int mobLevel = mobManager.getMobLevel(mob);
        User user = cacheManager.getUser(killer);

        if (user != null) {
            // XP e Ouro
            long xpGain = 10 + (mobLevel * 5L);
            long coinsGain = random.nextInt(5) + mobLevel;

            user.setExperience(user.getExperience() + xpGain);
            user.setCoins(user.getCoins() + coinsGain);

            // FIXME
            // Feedback Action Bar
            killer.sendActionBar(Component.text("+ " + xpGain + " XP  |  + " + coinsGain + " Ouro")
                    .color(NamedTextColor.GOLD));

            if (mobLevel >= 5) {
                killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1f);
            }

            // Força atualização do Scoreboard/Cache
            cacheManager.forceUpdate(killer);
            scoreboardManager.updateScoreboard(killer);
        }

        // SISTEMA DE LOOT (Hardcoded por enquanto)
        handleCustomLoot(event, mob.getType(), mobLevel, killer);
    }

    private void handleCustomLoot(EntityDeathEvent event, EntityType type, int level, Player killer) {
        // Limpa drops vanilla (ex: carne podre)
         event.getDrops().clear();

        // Chance base: 10% + 2% por nível
        double chance = 0.10 + (level * 0.02);

        if (random.nextDouble() > chance) return; // Azar, não dropou nada raro

        ItemStack drop = null;

        // Tabela de Loot Provisória
        switch (type) {
            case ZOMBIE:
                if (level >= 8) drop = new ItemStack(Material.IRON_INGOT, 2);
                else drop = new ItemStack(Material.IRON_NUGGET, 3);
                break;

            case SKELETON:
                if (level >= 8) drop = new ItemStack(Material.BOW); // TODO: Pode ser encantado
                else drop = new ItemStack(Material.ARROW, 5);
                break;

            case SPIDER:
                drop = new ItemStack(Material.STRING, 4);
                break;

            case CREEPER:
                if (level >= 5) drop = new ItemStack(Material.GUNPOWDER, 5);
                break;
        }

        if (drop != null) {
            // Auto-Loot (Direto no Inventário)
            HashMap<Integer, ItemStack> sobras = killer.getInventory().addItem(drop);

            // Se o inventário estiver cheio, dropa no chão a sobra
            if (!sobras.isEmpty()) {
                for (ItemStack item : sobras.values()) {
                    killer.getWorld().dropItemNaturally(killer.getLocation(), item);
                }
                killer.sendMessage(Component.text("Seu inventário está cheio! Item caiu no chão.")
                        .color(NamedTextColor.RED));
                // Efeito visual de drop raro
                if (level >= 8) {
                    event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1f);
                }
            } else {
                // Feedback visual de que recebeu item
                killer.playSound(killer.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1f);
                // FIXME
                killer.sendActionBar(Component.text("Você recebeu: " + drop.getType().name())
                        .color(NamedTextColor.GREEN));
            }

        }
    }
}