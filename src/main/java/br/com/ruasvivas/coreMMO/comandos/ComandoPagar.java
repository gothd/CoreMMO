package br.com.ruasvivas.coreMMO.comandos;

import br.com.ruasvivas.coreMMO.CoreMMO;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ComandoPagar implements CommandExecutor {

    private final CoreMMO plugin;

    public ComandoPagar(CoreMMO plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String label,
            @NotNull String[] args
    ) {

        if (sender instanceof Player pagador) {
            // Validação: /pagar <nick> <valor>
            if (args.length < 2) {
                pagador.sendMessage(Component.text("Uso: /pagar <jogador> <valor>").color(NamedTextColor.RED));
                return true;
            }

            Player alvo = Bukkit.getPlayer(args[0]);
            if (alvo == null) {
                pagador.sendMessage(Component.text("Jogador offline.").color(NamedTextColor.RED));
                return true;
            }

            try {
                long valor = Long.parseLong(args[1]);

                // Chama o Banqueiro para fazer a mágica
                boolean sucesso = plugin.getGerenteEconomia().pagar(pagador.getUniqueId(), alvo.getUniqueId(), valor);

                if (sucesso) {
                    pagador.sendMessage(Component.text("Pagamento enviado!").color(NamedTextColor.GREEN));
                    alvo.sendMessage(Component.text("Você recebeu " + valor + " moedas!").color(NamedTextColor.GOLD));
                } else {
                    pagador.sendMessage(
                            Component.text("Erro: Saldo insuficiente.")
                                    .color(NamedTextColor.RED)
                    );
                }

            } catch (NumberFormatException e) {
                pagador.sendMessage(Component.text("Valor inválido.").color(NamedTextColor.RED));
            }
            return true;
        }
        return false;
    }
}