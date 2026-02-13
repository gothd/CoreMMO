package br.com.ruasvivas.gameplay.listener;

import br.com.ruasvivas.common.model.User;
import br.com.ruasvivas.gameplay.manager.CacheManager;
import br.com.ruasvivas.gameplay.manager.DamageTrackerManager;
import br.com.ruasvivas.gameplay.manager.LootManager;
import br.com.ruasvivas.gameplay.manager.MobManager;
import br.com.ruasvivas.gameplay.ui.ScoreboardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

public class MobListener implements Listener {

    private final MobManager mobManager;
    private final CacheManager cacheManager;
    private final ScoreboardManager scoreboardManager;
    private final LootManager lootManager;
    private final DamageTrackerManager damageTracker;
    private final Random random = new Random();

    public MobListener(MobManager mobManager, CacheManager cacheManager, ScoreboardManager scoreboardManager, LootManager lootManager, DamageTrackerManager damageTracker) {
        this.mobManager = mobManager;
        this.cacheManager = cacheManager;
        this.scoreboardManager = scoreboardManager;
        this.lootManager = lootManager;
        this.damageTracker = damageTracker;
    }

    // --- RASTREAMENTO DE DANO ---
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof LivingEntity mob && event.getDamager() instanceof Player player) {
            // Só rastreia se for um Mob do sistema (tem nível)
            if (mob.getPersistentDataContainer().has(mobManager.LEVEL_KEY, PersistentDataType.INTEGER)) {
                damageTracker.registerDamage(mob, player, event.getFinalDamage());
            }
        }
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
        event.getEntity().customName(null); // Anti-Spam console
        event.getEntity().setCustomNameVisible(false);
        event.getDrops().clear(); // Limpa drops vanilla imediatamente

        LivingEntity mob = event.getEntity();

        // --- LIMPEZA DE COMBATE EXPIRADO ---
        // Remove jogadores que não bateram nos últimos 20 segundos
        damageTracker.removeExpiredAttackers(mob, lootManager.getDamageExpirationSeconds());

        Player killer = mob.getKiller(); // Last Hit Player (Pode ser nulo se o mob morrer queimado/por queda)

        // Validação extra: Se o killer "oficial" do Bukkit não está no tracker (porque expirou),
        // ele perde o status de killer para fins de loot.
        if (killer != null) {
            Map<UUID, Double> activeDamagers = damageTracker.getDamageMap(mob);
            if (!activeDamagers.containsKey(killer.getUniqueId())) {
                killer = null; // Killer expirou
            }
        }

        // --- LÓGICA DE ASSASSINO VIRTUAL (Morte Ambiental ou Killer Expirado) ---
        // O "Top Damager" assume a autoria da morte.
        if (killer == null) {
            UUID topDamagerUUID = damageTracker.getTopDamager(mob);
            if (topDamagerUUID != null) {
                Player topPlayer = Bukkit.getPlayer(topDamagerUUID);
                // Só assume se o jogador estiver elegível (perto e vivo)
                if (isEligibleForLoot(topPlayer, mob)) {
                    killer = topPlayer;
                }
            }
        }

        // Se após a limpeza e validação não sobrou ninguém...
        // Então ninguém ganha nada (morte 100% natural)
        if (killer == null && damageTracker.getDamageMap(mob).isEmpty()) {
            damageTracker.clearMob(mob);
            return; // Loot foi para o "limbo"
        }

        int mobLevel = mobManager.getMobLevel(mob);
        // Lista de quem vai receber loot
        Set<Player> recipients = new HashSet<>();

        // LÓGICA DE SELEÇÃO DE JOGADORES
        switch (lootManager.getLootMode()) {
            case LAST_HIT:
                // Mesmo no Last Hit, verifica se o killer não morreu ou sumiu no mesmo tick
                // "killer" pode ser quem deu o último hit OU quem bateu mais
                if (isEligibleForLoot(killer, mob)) {
                    recipients.add(killer);
                }
                break;

            case CONTRIBUTION:
                // TOP X PLAYERS
                Map<UUID, Double> damageMap = damageTracker.getDamageMap(mob);
                if (!damageMap.isEmpty()) {
                    List<UUID> topAttackers = damageMap.entrySet().stream()
                            // FILTRO DE DISTÂNCIA E MUNDO
                            // Filtramos ANTES de ordenar. Se o Top 1 fugiu, ele sai da lista aqui.
                            .filter(entry -> {
                                Player p = Bukkit.getPlayer(entry.getKey());
                                return isEligibleForLoot(p, mob);
                            })
                            // ORDENAÇÃO E LIMITE
                            .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed()) // Ordena Maior -> Menor
                            .limit(lootManager.getTopDamageLimit()) // Pega os X primeiros que SOBRARAM
                            .map(Map.Entry::getKey)
                            .toList();

                    for (UUID uuid : topAttackers) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) recipients.add(p);
                    }
                } else if (killer != null) {
                    // Fallback se algo der errado no tracker
                    recipients.add(killer);
                }
                break;

            case INSTANTIATED:
                // Verifica se atributo existe
                AttributeInstance maxHpAttr = mob.getAttribute(Attribute.MAX_HEALTH);
                double maxHealth = (maxHpAttr != null) ? maxHpAttr.getValue() : 100.0;
                // Evita divisão por zero
                if (maxHealth <= 0) maxHealth = 1.0;

                Map<UUID, Double> instMap = damageTracker.getDamageMap(mob);
                for (Map.Entry<UUID, Double> entry : instMap.entrySet()) {
                    Player p = Bukkit.getPlayer(entry.getKey());

                    // Verifica elegibilidade PRIMEIRO
                    if (!isEligibleForLoot(p, mob)) continue;

                    double percentage = entry.getValue() / maxHealth;
                    // Verifica se bateu o mínimo configurado (ex: 10%)
                    if (percentage >= lootManager.getMinDamageThreshold()) {
                        recipients.add(p);
                    }
                }
                // Fallback para o killer se ninguém qualificado sobrou
                if (recipients.isEmpty() && isEligibleForLoot(killer, mob)) {
                    recipients.add(killer);
                }
                break;
        }

        // --- ENTREGA DE RECOMPENSAS (Loop para cada jogador) ---
        for (Player player : recipients) {
            giveRewards(player, mob.getType(), mobLevel);
        }

        // Limpa a memória do tracker
        damageTracker.clearMob(mob);
    }

    /**
     * Verifica se o jogador está apto a receber loot (Vivo, Online, Perto e no Mesmo Mundo).
     */
    private boolean isEligibleForLoot(Player player, LivingEntity mob) {
        if (player == null || !player.isOnline()) return false;
        if (player.isDead()) return false; // Se morreu durante o boss, perde o loot

        // Verifica Mundo
        if (!player.getWorld().getUID().equals(mob.getWorld().getUID())) return false;

        // Verifica Distância
        double distanceSquared = player.getLocation().distanceSquared(mob.getLocation());
        double maxDist = lootManager.getMaxLootDistance();

        // Usa distanceSquared para performance (evita raiz quadrada)
        return distanceSquared <= (maxDist * maxDist);
    }

    private void giveRewards(Player player, EntityType type, int level) {
        User user = cacheManager.getUser(player);
        if (user == null) return;

        // --- Lógica de XP e Ouro (Individual para cada um) ---
        long xpGain = 10 + (level * 5L);
        long coinsGain = random.nextInt(5) + level;

        user.setExperience(user.getExperience() + xpGain);
        user.setCoins(user.getCoins() + coinsGain);

        if (level >= 5) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1f);
        }
        scoreboardManager.updateScoreboard(player);

        // --- Geração de Loot (Cada um roda seu RNG, sorte individual) ---
        List<ItemStack> drops = lootManager.getDropsFor(type, level);

        if (drops.isEmpty()) {
            Component msg = Component.text("+ " + xpGain + " XP  |  + " + coinsGain + " Ouro", NamedTextColor.GOLD);
            cacheManager.sendWarning(player, msg);
            return;
        }

        // --- Construção de Componentes ---
        TextComponent.Builder itemsMsgBuilder = Component.text();
        boolean bagFull = false;
        boolean rareDrop = false;
        boolean first = true;

        for (ItemStack item : drops) {
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) rareDrop = true;

            HashMap<Integer, ItemStack> sobras = player.getInventory().addItem(item);
            if (!sobras.isEmpty()) {
                bagFull = true;
                for (ItemStack sobra : sobras.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), sobra);
                }
            }

            if (!first) itemsMsgBuilder.append(Component.text(", ", NamedTextColor.GRAY));

            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                itemsMsgBuilder.append(Objects.requireNonNull(item.getItemMeta().displayName()));
            } else {
                itemsMsgBuilder.append(Component.translatable(item.getType()).color(NamedTextColor.AQUA));
            }
            first = false;
        }

        // Feedback
        if (bagFull) player.sendMessage(Component.text("Mochila cheia!").color(NamedTextColor.RED));
        else player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1f);

        if (rareDrop) player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1f);

        Component message = Component.text("Obteve: ", NamedTextColor.GREEN)
                .append(itemsMsgBuilder.build())
                .append(Component.text(" (+ " + xpGain + " XP)", NamedTextColor.GRAY));

        cacheManager.sendWarning(player, message);
    }

    // Auxiliares para organizar
    private boolean isHumanoid(EntityType type) {
        return type == EntityType.ZOMBIE || type == EntityType.SKELETON ||
                type == EntityType.PIGLIN || type == EntityType.WITHER_SKELETON;
    }
}