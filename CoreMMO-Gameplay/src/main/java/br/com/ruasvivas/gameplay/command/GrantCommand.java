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

public class GrantCommand implements CommandExecutor {

    private final PermissionManager permissionManager;

    // Injeção via Construtor
    public GrantCommand(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // OPs possuem essa permissão automaticamente por padrão.
        if (!sender.hasPermission("coremmo.admin")) {
            sender.sendMessage(Component.text("Sem permissão.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Uso: /grant <jogador> <permissao>").color(NamedTextColor.RED));
            return true;
        }

        String targetName = args[0];
        String permission = args[1];

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        // Adiciona no banco
        permissionManager.addPermission(target.getUniqueId(), permission);

        sender.sendMessage(Component.text("Permissão ")
                .append(Component.text(permission).color(NamedTextColor.GOLD))
                .append(Component.text(" adicionada para " + targetName + ". (Relogue para aplicar)")));

        return true;
    }
}