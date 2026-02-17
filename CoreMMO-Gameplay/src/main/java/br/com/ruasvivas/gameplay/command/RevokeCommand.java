package br.com.ruasvivas.gameplay.command;

import br.com.ruasvivas.gameplay.manager.PermissionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class RevokeCommand implements CommandExecutor {

    private final PermissionManager permissionManager;

    // Injeção via Construtor
    public RevokeCommand(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("coremmo.admin")) {
            sender.sendMessage(Component.text("Sem permissão.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Uso: /revoke <jogador> <permissao>").color(NamedTextColor.RED));
            return true;
        }

        String targetName = args[0];
        String permission = args[1];

        // Suporta offline players
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        permissionManager.removePermission(target.getUniqueId(), permission);

        sender.sendMessage(Component.text("Permissão ")
                .append(Component.text(permission).color(NamedTextColor.RED))
                .append(Component.text(" removida de " + targetName + ".")));

        return true;
    }
}