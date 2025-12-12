package br.com.ruasvivas.coreMMO.comandos;

import br.com.ruasvivas.coreMMO.menus.MenuClasses;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ComandoClasse implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player jogador) {
            // Abre o menu visual
            jogador.openInventory(MenuClasses.criar());
            return true;
        }

        return false;
    }
}