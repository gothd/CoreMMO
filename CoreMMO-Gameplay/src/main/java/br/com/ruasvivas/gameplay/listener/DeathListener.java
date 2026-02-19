package br.com.ruasvivas.gameplay.listener;

import br.com.ruasvivas.api.CoreRegistry;
import br.com.ruasvivas.common.model.User;
import br.com.ruasvivas.gameplay.CorePlugin;
import br.com.ruasvivas.gameplay.manager.CacheManager;
import br.com.ruasvivas.gameplay.ui.ScoreboardManager;
import br.com.ruasvivas.gameplay.util.BukkitConstants;
import br.com.ruasvivas.gameplay.util.StatHelper;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Iterator;

public class DeathListener implements Listener {

    private final CorePlugin plugin;
    private final CacheManager cacheManager;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

    public DeathListener(CorePlugin plugin, CacheManager cacheManager) {
        this.plugin = plugin;
        this.cacheManager = cacheManager;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        User user = cacheManager.getUser(player);
        if (user == null) return;

        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("death-penalty.enabled", true)) return;

        // --- PERDA DE XP ---
        double xpPercent = config.getDouble("death-penalty.xp-loss-percentage", 0.10);
        boolean allowLevelDown = config.getBoolean("death-penalty.allow-level-down", false);
        int oldLevel = user.getLevel();

        long lostXp = user.removeExperience(xpPercent, allowLevelDown);

        // Feedback de XP
        if (lostXp > 0) {
            String msg = config.getString("death-penalty.messages.death", "&cVocê morreu e perdeu {xp} XP.")
                    .replace("{xp}", String.valueOf(lostXp));
            player.sendMessage(serializer.deserialize(msg));
        }

        // Feedback de Level Down
        if (user.getLevel() < oldLevel) {
            String msg = config.getString("death-penalty.messages.level-down", "&cVocê caiu para o nível {level}!")
                    .replace("{level}", String.valueOf(user.getLevel()));
            player.sendMessage(serializer.deserialize(msg));

            // Se caiu de nível, precisa recalcular Vida/Mana máximas
            StatHelper.syncStats(player, user);
        }

        // Atualiza Scoreboard/ActionBar
        CoreRegistry.get(ScoreboardManager.class).updateScoreboard(player);
        cacheManager.updateActionBar(player);

        // --- SISTEMA DE DROP INTELIGENTE (Soulbound) ---
        if (config.getBoolean("death-penalty.keep-rpg-items", true)) {
            Iterator<ItemStack> dropsIterator = event.getDrops().iterator();

            while (dropsIterator.hasNext()) {
                ItemStack item = dropsIterator.next();

                // Verifica se é um item RPG do gerador
                if (item != null && item.hasItemMeta()) {
                    // Identifica itens gerados pelo CoreMMO pela chave
                    boolean isRpgItem = item.getItemMeta().getPersistentDataContainer()
                            .has(BukkitConstants.RPG_ITEM_KEY, PersistentDataType.STRING);

                    if (isRpgItem) {
                        // Move dos drops para a lista de itens mantidos
                        event.getItemsToKeep().add(item);
                        // Remove do chão para não duplicar
                        dropsIterator.remove();
                    }
                }
            }
        }

        // Opcional: Se quiser limpar o XP vanilla
        event.setDroppedExp(0);
    }
}