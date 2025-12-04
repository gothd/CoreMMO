package br.com.ruasvivas.coreMMO.eventos;

import br.com.ruasvivas.coreMMO.CoreMMO;
import br.com.ruasvivas.coreMMO.model.DadosJogador;
import br.com.ruasvivas.coreMMO.placar.GerentePlacar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.World;
import org.bukkit.Location;

import java.time.Duration;

import static org.bukkit.Bukkit.getWorld;

public class EntradaJornada implements Listener {

    private final CoreMMO plugin;

    public EntradaJornada(CoreMMO plugin) {
        this.plugin = plugin;
    }

    // @EventHandler é OBRIGATÓRIO. Sem isso, o servidor ignora o código.
    @EventHandler
    public void aoEntrar(PlayerJoinEvent evento) {
        Player jogador = evento.getPlayer();

        // --- ACESSO AO BANCO (Assíncrono) ---
        // Sai da linha principal para não travar o jogo
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {

            // 1. Garante registro no banco
            plugin.getJogadorDAO().criarJogador(jogador);

            // 2. Carrega os dados para a memória local (variável)
            DadosJogador dados = plugin.getJogadorDAO().carregarJogador(jogador.getUniqueId());

            // 3. Volta para a Thread Principal para salvar no Cache e Teleportar
            // (Ações que mexem no Bukkit precisam ser Sync)
            if (dados != null) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {

                    // Salva no Cache
                    plugin.getGerenteDados().adicionarJogador(dados);

                    // Teleporte (Se tiver local salvo)
                    if (dados.getMundo() != null) {
                        World mundo = getWorld(dados.getMundo());
                        if (mundo != null) {
                            jogador.teleport(new Location(mundo, dados.getX(), dados.getY(), dados.getZ(),
                                    dados.getYaw(), dados.getPitch()));
                        }
                    }

                    plugin.getLogger().info("Dados carregados para " + jogador.getName());
                });
            }
        });

        // 1. Mensagem no Chat (roda na hora)
        jogador.sendMessage(Component.text("Bem-vindo à sua lenda!").color(NamedTextColor.AQUA));

        // 2. Título na Tela (Recurso visual forte)
        Component titulo = Component.text("BEM-VINDO").color(NamedTextColor.GOLD);
        Component subtitulo = Component.text("Prepare-se, " + jogador.getName());

        // Definindo a duração (Aparece, Fica, Some)
        Title.Times tempos = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000),
                Duration.ofMillis(1000));

        Title tituloFinal = Title.title(titulo, subtitulo, tempos);
        jogador.showTitle(tituloFinal);

        // 3. Efeito Sonoro (Sound Design)
        // Toca o som na posição do jogador
        // 1.0f = Volume Máximo, 1.0f = Velocidade Normal
        jogador.playSound(jogador.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);

        new GerentePlacar().criarPlacar(jogador);
    }
}
