package br.com.ruasvivas.gameplay.manager;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SelectionManager {

    private final JavaPlugin plugin;
    // Guarda as posições: array de 2 (Pos1, Pos2)
    private final Map<UUID, Location[]> selections = new ConcurrentHashMap<>();
    // Guarda o ID do Timer ativo para podermos cancelá-lo
    private final Map<UUID, Integer> activeTimers = new ConcurrentHashMap<>();

    public SelectionManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setPos1(Player player, Location loc) {
        Location[] points = selections.computeIfAbsent(player.getUniqueId(), k -> new Location[2]);
        points[0] = loc;
        updateVisualizer(player);
    }

    public void setPos2(Player player, Location loc) {
        Location[] points = selections.computeIfAbsent(player.getUniqueId(), k -> new Location[2]);
        points[1] = loc;
        updateVisualizer(player);
    }

    public Location getPos1(UUID uuid) {
        Location[] points = selections.get(uuid);
        return points != null ? points[0] : null;
    }

    public Location getPos2(UUID uuid) {
        Location[] points = selections.get(uuid);
        return points != null ? points[1] : null;
    }

    public boolean hasFullSelection(UUID uuid) {
        return getPos1(uuid) != null && getPos2(uuid) != null;
    }

    public void clearSelection(UUID uuid) {
        selections.remove(uuid);
        Integer taskId = activeTimers.remove(uuid);
        if (taskId != null) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
    }

    // --- LÓGICA DO TIMER VISUAL (WORLDEDIT CUI STYLE) ---
    private void updateVisualizer(Player player) {
        UUID uuid = player.getUniqueId();
        Location[] points = selections.get(uuid);

        // Cancela o timer antigo se ele tentar marcar de novo
        Integer oldTask = activeTimers.remove(uuid);
        if (oldTask != null) plugin.getServer().getScheduler().cancelTask(oldTask);

        // Se não tem as duas posições ou se estão em mundos diferentes, cancela a visualização
        if (points[0] == null || points[1] == null || !points[0].getWorld().equals(points[1].getWorld())) return;

        // Inicia o Timer (BukkitRunnable)
        int taskId = new BukkitRunnable() {
            int ticksElapsed = 0;

            @Override
            public void run() {
                // Expira após 30 segundos (600 ticks) ou se o jogador deslogar
                if (!player.isOnline() || ticksElapsed >= 600) {
                    clearSelection(uuid);
                    player.sendMessage("§eSua seleção expirou.");
                    this.cancel();
                    return;
                }

                drawCuboidCorners(player, points[0], points[1]);
                ticksElapsed += 10;
            }
        }.runTaskTimer(plugin, 0L, 10L).getTaskId(); // Roda a cada meio segundo (10 ticks)

        activeTimers.put(uuid, taskId);
    }

    // Desenha partículas nos 8 cantos da Bounding Box selecionada
    private void drawCuboidCorners(Player player, Location loc1, Location loc2) {
        double minX = Math.min(loc1.getX(), loc2.getX());
        double minY = Math.min(loc1.getY(), loc2.getY());
        double minZ = Math.min(loc1.getZ(), loc2.getZ());

        // +1 para cobrir o bloco inteiro (Minecraft blocks são do ponto 0 até 1)
        double maxX = Math.max(loc1.getX(), loc2.getX()) + 1.0;
        double maxY = Math.max(loc1.getY(), loc2.getY()) + 1.0;
        double maxZ = Math.max(loc1.getZ(), loc2.getZ()) + 1.0;

        Location[] corners = new Location[]{
                new Location(loc1.getWorld(), minX, minY, minZ),
                new Location(loc1.getWorld(), maxX, minY, minZ),
                new Location(loc1.getWorld(), minX, maxY, minZ),
                new Location(loc1.getWorld(), maxX, maxY, minZ),
                new Location(loc1.getWorld(), minX, minY, maxZ),
                new Location(loc1.getWorld(), maxX, minY, maxZ),
                new Location(loc1.getWorld(), minX, maxY, maxZ),
                new Location(loc1.getWorld(), maxX, maxY, maxZ)
        };

        // Envia as partículas apenas para o jogador que selecionou
        for (Location corner : corners) {
            player.spawnParticle(Particle.HAPPY_VILLAGER, corner, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Estica a seleção do limite inferior ao limite superior do mundo.
     */
    public boolean expandVert(Player player) {
        UUID uuid = player.getUniqueId();
        Location[] points = selections.get(uuid);

        if (points == null || points[0] == null || points[1] == null) {
            return false; // Precisa ter os dois pontos marcados primeiro
        }

        // Limites do Minecraft Moderno (1.18+)
        points[0].setY(-64.0);
        points[1].setY(319.0);

        // Atualiza as partículas para mostrar a nova área gigantesca
        updateVisualizer(player);
        return true;
    }
}