package br.com.ruasvivas.gameplay.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KickCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Permissão de Moderador
        if (!sender.hasPermission("coremmo.mod")) {
            sender.sendMessage(Component.text("Sem permissão.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(Component.text("Uso: /kick <jogador> [motivo]").color(NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Jogador não encontrado ou offline.").color(NamedTextColor.RED));
            return true;
        }

        // Impede que um Mod kicke alguém com permissão de Admin
        if (target.hasPermission("coremmo.admin") && !sender.hasPermission("coremmo.admin")) {
            sender.sendMessage(Component.text("Você não pode expulsar um superior.").color(NamedTextColor.RED));
            return true;
        }

        // Monta o motivo (se houver)
        String reason = "Expulso pelo servidor.";
        if (args.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }
            reason = sb.toString().trim();
        }

        // Formatação da mensagem de desconexão (Tela Vermelha)
        Component kickMessage = Component.text("⛔ VOCÊ FOI EXPULSO ⛔\n\n", NamedTextColor.RED)
                .append(Component.text(reason, NamedTextColor.WHITE))
                .append(Component.text("\n\nCoreMMO Staff", NamedTextColor.GRAY));

        target.kick(kickMessage);

        sender.sendMessage(Component.text("Expulsou ")
                .append(Component.text(target.getName()).color(NamedTextColor.GOLD))
                .append(Component.text(": " + reason, NamedTextColor.GRAY)));

        // Broadcast para a staff (opcional, aqui manda global para todos saberem)
        Bukkit.broadcast(Component.text("⚠ " + target.getName() + " foi expulso: " + reason, NamedTextColor.RED));

        return true;
    }
}