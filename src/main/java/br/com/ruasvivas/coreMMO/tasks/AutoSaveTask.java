package br.com.ruasvivas.coreMMO.tasks;

import br.com.ruasvivas.coreMMO.CoreMMO;
import br.com.ruasvivas.coreMMO.model.DadosJogador;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class AutoSaveTask extends BukkitRunnable {

    private final CoreMMO plugin;

    public AutoSaveTask(CoreMMO plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Lista temporária para o Snapshot
        List<DadosJogador> paraSalvar = new ArrayList<>();

        // ETAPA 1: Snapshot (Thread Principal)
        // É obrigatório ler a Location aqui, na thread principal.
        for (Player jogador : Bukkit.getOnlinePlayers()) {

            // Busca os dados da memória (Cache)
            DadosJogador dados = plugin.getGerenteDados()
                    .getDados(jogador.getUniqueId());

            if (dados != null) {
                // Atualiza a posição no objeto antes de salvar
                dados.setLocalizacao(jogador.getLocation());

                // Adiciona na lista de processamento
                paraSalvar.add(dados);
            }
        }

        // Se não tem ninguém, não gasta recursos do banco
        if (paraSalvar.isEmpty()) return;

        plugin.getLogger().info("Auto-Save: Salvando "
                + paraSalvar.size()
                + " jogadores...");

        // ETAPA 2: Persistência (Thread Assíncrona)
        // Agora podemos demorar o tempo que for necessário no banco
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {

            for (DadosJogador dados : paraSalvar) {
                // Reutilizamos o método UPDATE da DAO
                plugin.getJogadorDAO().salvarJogador(dados);
            }

        });
    }
}