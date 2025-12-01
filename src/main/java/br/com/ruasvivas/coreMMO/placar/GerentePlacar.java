package br.com.ruasvivas.coreMMO.placar;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class GerentePlacar {

    public void criarPlacar(Player jogador) {
        // 1. Conseguindo uma prancheta em branco
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        // 2. Criando o Título
        Component tituloVisual = Component.text("MMORPG").color(NamedTextColor.GOLD);

        // TRADUÇÃO: Convertendo Componente Moderno -> Texto Antigo (§6MMORPG)
        String tituloAntigo = LegacyComponentSerializer.legacySection().serialize(tituloVisual);

        Objective objetivo = board.registerNewObjective("sidebar", Criteria.DUMMY, Component.text(tituloAntigo));
        objetivo.setDisplaySlot(DisplaySlot.SIDEBAR);

        // 3. Criando as linhas (Score)
        // Linha 2: Nome do Jogador
        Component textoJogador = Component.text("Jogador: ")
                .color(NamedTextColor.WHITE)
                .append(Component.text(jogador.getName()).color(NamedTextColor.GREEN));

        Score linha2 = objetivo.getScore(LegacyComponentSerializer.legacySection().serialize(textoJogador));
        linha2.setScore(2); // Posição na lista

        // Linha 1: Site
        Component textoSite = Component.text("www.seuserver.com").color(NamedTextColor.GRAY);

        Score linha1 = objetivo.getScore(LegacyComponentSerializer.legacySection().serialize(textoSite));
        linha1.setScore(1);

        // 4. Entregando o placar
        jogador.setScoreboard(board);
    }
}