package br.com.ruasvivas.gameplay.command;

import br.com.ruasvivas.common.model.User;
import br.com.ruasvivas.gameplay.manager.CacheManager;
import br.com.ruasvivas.gameplay.util.StatHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StatsCommand implements CommandExecutor {

    private final CacheManager cacheManager;

    public StatsCommand(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        User user = cacheManager.getUser(player);
        if (user == null) return true;

        double rpgArmor = StatHelper.getPlayerTotalArmor(player);
        // Calcula a mitigação atual para mostrar ao jogador (transparência)
        double mitigation = (1.0 - (100.0 / (100.0 + rpgArmor))) * 100.0;

        player.sendMessage(Component.text(" "));
        player.sendMessage(Component.text("⚔ SEUS ATRIBUTOS ⚔", NamedTextColor.GOLD, TextDecoration.BOLD));
        player.sendMessage(Component.text(" "));

        player.sendMessage(Component.text(" Classe: ", NamedTextColor.GRAY)
                .append(Component.text(user.getRpgClass().getDisplayName(), NamedTextColor.YELLOW)));

        player.sendMessage(Component.text(" Nível: ", NamedTextColor.GRAY)
                .append(Component.text(user.getLevel(), NamedTextColor.GREEN))
                .append(Component.text(" (" + user.getProgressPercentage() + "%)", NamedTextColor.DARK_GRAY)));

        player.sendMessage(Component.text(" "));

        player.sendMessage(Component.text(" ❤ Vida: ", NamedTextColor.RED)
                .append(Component.text((int) player.getHealth() + "/" + (int) player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue(), NamedTextColor.WHITE)));

        player.sendMessage(Component.text(" ✨ Mana: ", NamedTextColor.AQUA)
                .append(Component.text((int) user.getMana() + "/" + (int) user.getMaxMana(), NamedTextColor.WHITE)));

        player.sendMessage(Component.text(" 🛡 Defesa: ", NamedTextColor.BLUE)
                .append(Component.text((int) rpgArmor, NamedTextColor.WHITE))
                .append(Component.text(" (Absorve " + String.format("%.1f", mitigation) + "%)", NamedTextColor.GRAY)));

        player.sendMessage(Component.text(" "));

        return true;
    }
}