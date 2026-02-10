package br.com.ruasvivas.gameplay.command;

import br.com.ruasvivas.gameplay.manager.LootManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final LootManager lootManager;

    public ReloadCommand(JavaPlugin plugin, LootManager lootManager) {
        this.plugin = plugin;
        this.lootManager = lootManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("coremmo.admin")) {
            sender.sendMessage(Component.text("Sem permissão.").color(NamedTextColor.RED));
            return true;
        }

        sender.sendMessage(Component.text("Recarregando configurações do CoreMMO...").color(NamedTextColor.YELLOW));

        long start = System.currentTimeMillis();

        // Recarrega o arquivo do disco para a memória do Bukkit
        plugin.reloadConfig();

        // Manda o LootManager reler a memória
        lootManager.loadConfig();

        long time = System.currentTimeMillis() - start;
        sender.sendMessage(Component.text("Configurações recarregadas em " + time + "ms!").color(NamedTextColor.GREEN));

        return true;
    }
}