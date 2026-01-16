package br.com.ruasvivas.gameplay.listener;

import br.com.ruasvivas.gameplay.manager.CacheManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CombatListener implements Listener {

    private final JavaPlugin plugin;
    private final CacheManager cacheManager;

    public CombatListener(JavaPlugin plugin, CacheManager cacheManager) {
        this.plugin = plugin;
        this.cacheManager = cacheManager;
    }

    // Use MONITOR para ler o resultado final após cálculos de armadura
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {

            // Agendamento: Espera 1 tick para o Bukkit aplicar o dano na vida
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // Força atualização visual (Prioridade Máxima)
                cacheManager.forceUpdate(player);
            }, 1L);
        }
    }
}