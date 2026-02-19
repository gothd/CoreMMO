package br.com.ruasvivas.gameplay.command;

import br.com.ruasvivas.gameplay.manager.ItemGenerator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GiveRPGCommand implements CommandExecutor {

    private final ItemGenerator itemGenerator;

    public GiveRPGCommand(ItemGenerator itemGenerator) {
        this.itemGenerator = itemGenerator;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Permissão de Admin
        if (!sender.hasPermission("coremmo.admin")) {
            sender.sendMessage(Component.text("Sem permissão.").color(NamedTextColor.RED));
            return true;
        }

        // Uso: /giverpg <jogador> <nivel_mob> [quantidade]
        if (args.length < 2) {
            sender.sendMessage(Component.text("Uso: /giverpg <jogador> <nivel_do_item> [qtd]").color(NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Jogador offline.").color(NamedTextColor.RED));
            return true;
        }

        int level;
        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Nível deve ser um número.").color(NamedTextColor.RED));
            return true;
        }

        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException ignored) {
            }
        }

        // Gera o item usando a lógica do ItemGenerator (com Tiers, Atributos, etc)
        // Como o gerador é aleatório (pode vir espada, bota, etc), gera 'amount' vezes.
        for (int i = 0; i < amount; i++) {
            ItemStack item = itemGenerator.generateLoot(level);
            if (item != null) {
                target.getInventory().addItem(item);
            }
        }

        target.playSound(target.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
        sender.sendMessage(Component.text("Gerado(s) " + amount + " item(ns) de nível " + level + " para " + target.getName()).color(NamedTextColor.GREEN));

        return true;
    }
}