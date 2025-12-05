package br.com.ruasvivas.coreMMO.tasks;

import br.com.ruasvivas.coreMMO.CoreMMO;
import br.com.ruasvivas.coreMMO.model.DadosJogador;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RegeneracaoTask extends BukkitRunnable {

    private final CoreMMO plugin;

    public RegeneracaoTask(CoreMMO plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player jogador : Bukkit.getOnlinePlayers()) {
            DadosJogador dados = plugin.getGerenteDados().getDados(jogador.getUniqueId());

            if (dados != null) {
                // 1. Lógica: Recupera 5% da mana total
                double regen = dados.getMaxMana() * 0.05;
                dados.setMana(dados.getMana() + regen);

                // 2. Visual: Atualiza a barra
                plugin.getGerenteDados().atualizarBarra(jogador, dados);
            }
        }
    }
}