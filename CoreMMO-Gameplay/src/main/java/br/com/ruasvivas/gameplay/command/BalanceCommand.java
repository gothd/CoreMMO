package br.com.ruasvivas.gameplay.command;

import br.com.ruasvivas.common.model.User;
import br.com.ruasvivas.gameplay.manager.CacheManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BalanceCommand implements CommandExecutor {

    private final CacheManager cacheManager;

    public BalanceCommand(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        User user = cacheManager.getUser(player);

        if (user != null) {
            player.sendMessage(Component.text("💰 Saldo: ").color(NamedTextColor.GREEN)
                    .append(Component.text(user.getCoins()).color(NamedTextColor.GOLD)));
        }
        return true;
    }
}