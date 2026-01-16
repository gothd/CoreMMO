package br.com.ruasvivas.gameplay.command;

import br.com.ruasvivas.api.service.EconomyService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PayCommand implements CommandExecutor {

    private final EconomyService economyService;

    public PayCommand(EconomyService economyService) {
        this.economyService = economyService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player payer)) return false;

        // Validação: /pagar <nick> <valor>
        if (args.length < 2) {
            payer.sendMessage(Component.text("Uso: /pagar <jogador> <valor>").color(NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            payer.sendMessage(Component.text("Jogador offline.").color(NamedTextColor.RED));
            return true;
        }

        try {
            long amount = Long.parseLong(args[1]);

            // Chama o EconomyManager (Interface)
            boolean success = economyService.pay(payer.getUniqueId(), target.getUniqueId(), amount);

            if (success) {
                payer.sendMessage(Component.text("Pagamento enviado!").color(NamedTextColor.GREEN));
                target.sendMessage(Component.text("Você recebeu " + amount + " moedas!").color(NamedTextColor.GOLD));
            } else {
                payer.sendMessage(Component.text("Erro: Saldo insuficiente ou valor inválido.").color(NamedTextColor.RED));
            }

        } catch (NumberFormatException e) {
            payer.sendMessage(Component.text("Valor inválido.").color(NamedTextColor.RED));
        }
        return true;
    }
}