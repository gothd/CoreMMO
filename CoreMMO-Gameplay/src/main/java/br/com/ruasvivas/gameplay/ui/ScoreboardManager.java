package br.com.ruasvivas.gameplay.ui;

import br.com.ruasvivas.api.CoreRegistry;
import br.com.ruasvivas.common.model.User;
import br.com.ruasvivas.gameplay.manager.CacheManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.Objects;

public class ScoreboardManager {

    private final String objS = "sidebar";
    private final CacheManager cacheManager;

    public ScoreboardManager() {
        this.cacheManager = CoreRegistry.get(CacheManager.class);
    }

    public void createScoreboard(Player player) {
        User user = cacheManager.getUser(player);
        if (user == null) return;

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective(objS, Criteria.DUMMY,
                Component.text("Ruas Vivas").color(NamedTextColor.GOLD));

        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Criamos os times para as linhas dinâmicas
        setupTeam(board, "coins", "§eOuro: ", "§f" + user.getCoins(), 3);
        setupTeam(board, "level", "§aNível: ", "§f" + user.getLevel(), 4);
        setupTeam(board, "class", "§bClasse: ", "§f" + user.getRpgClass().getDisplayName(), 5);

        // Linhas estáticas
        obj.getScore("§7www.ruasvivas.com.br").setScore(1);
        obj.getScore("§1 ").setScore(2); // Linha vazia

        player.setScoreboard(board);
    }

    public void updateScoreboard(Player player) {
        User user = cacheManager.getUser(player);
        if (user == null) return;

        Scoreboard board = player.getScoreboard();

        // Atualizamos apenas o sufixo dos times existentes
        updateTeamValue(board, "coins", "§f" + user.getCoins());
        updateTeamValue(board, "level", "§f" + user.getLevel());
        updateTeamValue(board, "class", "§f" + user.getRpgClass().getDisplayName());
    }

    private void setupTeam(Scoreboard board, String name, String prefix, String suffix, int score) {
        Team team = board.registerNewTeam(name);

        // Usamos uma cor legacy invisível como entrada única para o Score
        String entry = getEntryByScore(score);
        team.addEntry(entry);

        team.prefix(Component.text(prefix));
        team.suffix(Component.text(suffix));

        Objects.requireNonNull(board.getObjective(objS)).getScore(entry).setScore(score);
    }

    private void updateTeamValue(Scoreboard board, String teamName, String value) {
        Team team = board.getTeam(teamName);
        if (team != null) {
            team.suffix(Component.text(value));
        }
    }

    private String getEntryByScore(int score) {
        return "§" + score + "§r"; // Cria uma entrada invisível única baseada na posição
    }
}