package br.com.ruasvivas.coreMMO.comandos;

import br.com.ruasvivas.coreMMO.CoreMMO;
import br.com.ruasvivas.coreMMO.model.DadosJogador;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ComandoSaldo implements CommandExecutor {

    private final CoreMMO plugin;

    public ComandoSaldo(CoreMMO plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String label,
            @NotNull String[] args
    ) {

        if (sender instanceof Player jogador) {
            DadosJogador dados = plugin.getGerenteDados().getDados(jogador.getUniqueId());

            if (dados != null) {
                jogador.sendMessage(Component.text("💰 Saldo: ").color(NamedTextColor.GREEN)
                        .append(Component.text(dados.getMoedas()).color(NamedTextColor.GOLD)));
            }
            return true;
        }
        return false;
    }
}