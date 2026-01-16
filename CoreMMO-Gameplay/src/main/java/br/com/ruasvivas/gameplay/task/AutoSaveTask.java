package br.com.ruasvivas.gameplay.task;

import br.com.ruasvivas.api.CoreRegistry;
import br.com.ruasvivas.api.dao.UserDAO;
import br.com.ruasvivas.common.model.User;
import br.com.ruasvivas.gameplay.manager.CacheManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AutoSaveTask extends BukkitRunnable {

    private final JavaPlugin plugin;
    private final CacheManager cacheManager;
    private final Logger logger;

    public AutoSaveTask(JavaPlugin plugin, CacheManager cacheManager) {
        this.plugin = plugin;
        this.cacheManager = cacheManager;
        this.logger = CoreRegistry.getSafe(Logger.class).orElse(plugin.getLogger());
    }

    @Override
    public void run() {
        // Lista temporária para o Snapshot (Cópia dos dados)
        List<User> toSave = new ArrayList<>();

        // ETAPA 1: Snapshot (Thread Principal - Sync)
        // Precisamos ler a localização aqui, pois a API do Bukkit não é thread-safe
        for (Player player : Bukkit.getOnlinePlayers()) {
            User user = cacheManager.getUser(player);

            if (user != null) {
                // Atualiza a posição no objeto User antes de salvar
                Location loc = player.getLocation();
                user.setLocation(
                        loc.getWorld().getName(),
                        loc.getX(), loc.getY(), loc.getZ(),
                        loc.getYaw(), loc.getPitch()
                );

                toSave.add(user);
            }
        }

        if (toSave.isEmpty()) return;

        // ETAPA 2: Persistência (Thread Assíncrona - Async)
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {

            // Pega o DAO (Infra)
            CoreRegistry.getSafe(UserDAO.class).ifPresent(dao -> {
                int count = 0;
                for (User user : toSave) {
                    if (dao.saveUser(user)) {
                        count++;
                    }
                }
                // Log opcional (remover em produção para evitar spam)
                if (count > 0) logger.info(count + " jogadores salvos.");
            });

        });
    }
}