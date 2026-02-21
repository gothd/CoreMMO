package br.com.ruasvivas.gameplay.manager;

import br.com.ruasvivas.api.CoreRegistry;
import br.com.ruasvivas.api.dao.UserDAO;
import br.com.ruasvivas.api.service.PermissionService;
import br.com.ruasvivas.common.model.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PermissionManager implements PermissionService {

    private final JavaPlugin plugin;
    // Guarda o "Attachment" do Bukkit para poder remover depois
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public PermissionManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void removePermissions(Player player) {
        PermissionAttachment attachment = attachments.remove(player.getUniqueId());
        if (attachment != null) {
            player.removeAttachment(attachment);
        }
    }

    /**
     * Pega as permissões do User e aplica no Player do Bukkit.
     */
    public void injectPermissions(Player player, User user) {
        // Remove anterior se existir (re-login)
        removePermissions(player);

        PermissionAttachment attachment = player.addAttachment(plugin);

        // Aplica todas as permissões do banco
        for (String perm : user.getPermissions()) {
            // Se for '*', dá OP virtual
            if (perm.equals("*")) {
                player.setOp(true);
            } else {
                attachment.setPermission(perm, true);
            }
        }

        attachments.put(player.getUniqueId(), attachment);
    }

    @Override
    public void addPermission(UUID uuid, String permission) {
        String perm = permission.toLowerCase();

        // Atualiza Banco (Async) via DAO
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            CoreRegistry.getSafe(UserDAO.class).ifPresent(dao -> dao.addPermission(uuid, perm));
        });

        // Atualiza Jogador Online (Instantâneo)
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && attachments.containsKey(uuid)) {
            // Aplica no Bukkit
            PermissionAttachment attachment = attachments.get(uuid);
            if (perm.equals("*")) player.setOp(true);
            else attachment.setPermission(perm, true);

            // Atualiza Objeto User no Cache
            User user = CoreRegistry.get(CacheManager.class).getUser(uuid);
            if (user != null && !user.getPermissions().contains(perm)) {
                user.getPermissions().add(perm);
            }
        }
    }

    @Override
    public void removePermission(UUID uuid, String permission) {
        String perm = permission.toLowerCase();

        // Atualiza Banco (Async)
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            CoreRegistry.getSafe(UserDAO.class).ifPresent(dao -> dao.removePermission(uuid, perm));
        });

        // Atualiza Jogador Online
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && attachments.containsKey(uuid)) {
            // Remove do Bukkit
            PermissionAttachment attachment = attachments.get(uuid);
            if (perm.equals("*")) player.setOp(false);
            else attachment.unsetPermission(perm);

            // Remove do Cache
            User user = CoreRegistry.get(CacheManager.class).getUser(uuid);
            if (user != null) {
                user.getPermissions().remove(perm);
            }
        }
    }
}