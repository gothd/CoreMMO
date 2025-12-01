package br.com.ruasvivas.coreMMO.comandos;

// IMPORTS: As ferramentas que vamos usar

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public class ComandoCurar implements CommandExecutor {

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        // 1. Verificação de Segurança
        // Usamos "Pattern Matching" (instanceof Player jogador) para checar
        // se quem digitou é um humano e já criar a variável 'jogador'.
        if (sender instanceof Player jogador) {

            // 2. A Lógica da Cura
            jogador.setHealth(20);    // 20 pontos = 10 corações cheios
            jogador.setFoodLevel(20); // 20 pontos = Barra de comida cheia

            // 3. Feedback Visual
            // Usamos Componentes para criar textos coloridos
            jogador.sendMessage(Component.text("Você foi curado pela bênção antiga!").color(NamedTextColor.GREEN));

            return true; // Sucesso!
        }

        return false; // Falha (não era player)
    }
}