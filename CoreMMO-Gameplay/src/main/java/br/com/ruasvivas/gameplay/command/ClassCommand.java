package br.com.ruasvivas.gameplay.command;

import br.com.ruasvivas.gameplay.ui.ClassSelectionMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClassCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player player) {
            player.openInventory(ClassSelectionMenu.create());
            return true;
        }

        sender.sendMessage("Apenas jogadores podem escolher uma classe.");
        return false;
    }
}