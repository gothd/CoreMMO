package br.com.ruasvivas.gameplay.command;

import br.com.ruasvivas.api.CoreRegistry;
import br.com.ruasvivas.gameplay.manager.NPCManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class NPCCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("coremmo.admin")) {
            sender.sendMessage(Component.text("Sem permissão.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 2 || !args[0].equalsIgnoreCase("set")) {
            sender.sendMessage(Component.text("Uso: /npc set <id_do_config>").color(NamedTextColor.RED));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }

        String npcId = args[1].toLowerCase(); // IDs no YAML costumam ser minúsculos

        NPCManager manager = CoreRegistry.get(NPCManager.class);

        // Verifica se o NPC existe no config antes de setar
        if (!Objects.requireNonNull(player.getServer().getPluginManager().getPlugin("CoreMMO")).getConfig().contains("npcs." + npcId)) {
            player.sendMessage(Component.text("Erro: O NPC '" + npcId + "' não existe no config.yml.").color(NamedTextColor.RED));
            return true;
        }

        // Seta a posição exata onde o administrador está pisando e olhando
        manager.setNpcLocation(npcId, player.getLocation());

        player.sendMessage(Component.text("✅ Localização do NPC '")
                .append(Component.text(npcId).color(NamedTextColor.GOLD))
                .append(Component.text("' atualizada e salva no config!")));

        return true;
    }
}