package br.com.ruasvivas.gameplay.command;

import br.com.ruasvivas.api.CoreRegistry;
import br.com.ruasvivas.api.dao.GuildDAO;
import br.com.ruasvivas.api.dao.UserDAO;
import br.com.ruasvivas.common.model.Guild;
import br.com.ruasvivas.common.model.User;
import br.com.ruasvivas.gameplay.manager.CacheManager;
import br.com.ruasvivas.gameplay.manager.GuildManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuildCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final GuildManager guildManager;
    private final CacheManager cacheManager;

    // Mapa temporário para confirmação
    private final Map<UUID, Long> confirmDelete = new HashMap<>();

    public GuildCommand(JavaPlugin plugin, GuildManager guildManager, CacheManager cacheManager) {
        this.plugin = plugin;
        this.guildManager = guildManager;
        this.cacheManager = cacheManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("criar")) {
                handleCreate(player, args);
                return true;
            }
            if (args[0].equalsIgnoreCase("info")) {
                handleInfo(player, args);
                return true;
            }
            if (args[0].equalsIgnoreCase("deletar")) {
                handleDelete(player);
                return true;
            }
        }

        player.sendMessage(Component.text("Use: /guilda <criar|info|deletar>").color(NamedTextColor.YELLOW));
        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Component.text("/guilda criar <tag> <nome>").color(NamedTextColor.RED));
            return;
        }

        String tag = args[1];
        String name = joinArgs(args, 2);
        long cost = 5000;

        User user = cacheManager.getUser(player);
        if (user == null) return;

        if (user.hasGuild()) {
            player.sendMessage(Component.text("Você já tem guilda!").color(NamedTextColor.RED));
            return;
        }

        if (user.getCoins() < cost) {
            player.sendMessage(Component.text("Custa " + cost + " moedas.").color(NamedTextColor.RED));
            return;
        }

        // --- Criação Async ---
        Guild newGuild = new Guild(name, tag, player.getUniqueId());

        // Cobrança (Sync na memória)
        user.setCoins(user.getCoins() - cost);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            GuildDAO dao = CoreRegistry.get(GuildDAO.class);
            dao.createGuild(newGuild);

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (newGuild.getId() > 0) {
                    guildManager.registerGuild(newGuild);
                    user.setGuildId(newGuild.getId());

                    // Salva jogador atualizado (Async)
                    saveUserAsync(user);

                    player.sendMessage(Component.text("Guilda criada!").color(NamedTextColor.GREEN));
                } else {
                    player.sendMessage(Component.text("Erro ao criar guilda (Nome/Tag já existem?)").color(NamedTextColor.RED));
                    // Devolve o dinheiro se falhar
                    user.setCoins(user.getCoins() + cost);
                }
            });
        });
    }

    private void handleInfo(Player player, String[] args) {
        String search = (args.length > 1) ? joinArgs(args, 1) : "";
        Guild target = guildManager.getByNameOrTag(search);

        if (target == null) {
            // Se não digitou nada, tenta mostrar a própria guilda
            User user = cacheManager.getUser(player);
            if (user.hasGuild()) {
                target = guildManager.getById(user.getGuildId());
            }

            if (target == null) {
                player.sendMessage(Component.text("Guilda não encontrada.").color(NamedTextColor.RED));
                return;
            }
        }

        player.sendMessage(Component.text("Carregando estatísticas...").color(NamedTextColor.GRAY));

        // Busca stats no banco (Async)
        Guild finalTarget = target;
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            CoreRegistry.get(GuildDAO.class).updateStatistics(finalTarget);

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.sendMessage(Component.text("Guilda: " + finalTarget.getName()).color(NamedTextColor.GOLD));
                player.sendMessage(Component.text("KDR: " + String.format("%.2f", finalTarget.getKDR())).color(NamedTextColor.YELLOW));
                player.sendMessage(Component.text("Membros: " + finalTarget.getMemberCount() + "/" + finalTarget.getMaxMembers()).color(NamedTextColor.AQUA));
            });
        });
    }

    private void handleDelete(Player player) {
        User user = cacheManager.getUser(player);
        if (!user.hasGuild()) {
            player.sendMessage(Component.text("Você não tem guilda.").color(NamedTextColor.RED));
            return;
        }

        Guild guild = guildManager.getById(user.getGuildId());
        if (guild == null || !guild.getLeaderUuid().equals(player.getUniqueId())) {
            player.sendMessage(Component.text("Apenas o líder pode deletar.").color(NamedTextColor.RED));
            return;
        }

        UUID uuid = player.getUniqueId();
        if (!confirmDelete.containsKey(uuid)) { //
            confirmDelete.put(uuid, System.currentTimeMillis());
            player.sendMessage(Component.text("⚠️ Confirme digitando novamente.").color(NamedTextColor.RED));

            // Limpa confirmação após 10 segundos
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> confirmDelete.remove(uuid), 200L);
            return;
        }
        confirmDelete.remove(uuid);

        int guildId = guild.getId();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            CoreRegistry.get(GuildDAO.class).deleteGuild(guildId); //

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                guildManager.unregisterGuild(guild);

                // Atualiza jogadores online
                for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                    User u = cacheManager.getUser(p);
                    if (u != null && u.getGuildId() == guildId) {
                        u.setGuildId(0);
                        p.sendMessage(Component.text("Guilda dissolvida!").color(NamedTextColor.RED));
                    }
                }
            });
        });
    }

    private String joinArgs(String[] args, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i > start) sb.append(" ");
            sb.append(args[i]);
        }
        return sb.toString();
    }

    private void saveUserAsync(User user) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                CoreRegistry.getSafe(UserDAO.class).ifPresent(dao -> dao.saveUser(user))
        );
    }
}