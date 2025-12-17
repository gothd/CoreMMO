package br.com.ruasvivas.coreMMO.eventos;

import br.com.ruasvivas.coreMMO.CoreMMO;
import br.com.ruasvivas.coreMMO.model.DadosJogador;
import br.com.ruasvivas.coreMMO.placar.GerentePlacar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;

import static org.bukkit.Bukkit.getWorld;

public class EntradaJornada implements Listener {

    private final CoreMMO plugin;

    public EntradaJornada(CoreMMO plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void aoEntrar(PlayerJoinEvent evento) {
        Player jogador = evento.getPlayer();

        // --- ACESSO AO BANCO (Assíncrono) ---
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {

            // 1. Garante/Cria jogador no banco
            plugin.getJogadorDAO().criarJogador(jogador);

            // 2. Carrega os dados para a memória
            DadosJogador dados = plugin.getJogadorDAO().carregarJogador(jogador.getUniqueId());

            // 3. Volta para a Thread Principal (Sync) para liberar o jogador
            if (dados != null) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {

                    // A. Salva no Cache
                    plugin.getGerenteDados().adicionarJogador(dados);

                    // B. Teleporte (Se houver local salvo)
                    if (dados.getMundo() != null) {
                        World mundo = getWorld(dados.getMundo());
                        if (mundo != null) {
                            jogador.teleport(new Location(mundo, dados.getX(), dados.getY(), dados.getZ(),
                                    dados.getYaw(), dados.getPitch()));
                        }
                    }

                    // --- C. A LIBERAÇÃO ---
                    // Remove o "Stun" e a Cegueira
                    jogador.removePotionEffect(PotionEffectType.BLINDNESS);
                    jogador.removePotionEffect(PotionEffectType.SLOWNESS);
                    jogador.removePotionEffect(PotionEffectType.RESISTANCE);

                    // Avisa o gerente que acabou o carregamento
                    // (Libera Dano/Movimento)
                    plugin.getGerenteDados().setCarregando(jogador.getUniqueId(), false);

                    // Atualiza a UI imediatamente (Vida/Mana)
                    plugin.getGerenteDados().atualizarBarra(jogador);

                    // --- D. FEEDBACK VISUAL ---
                    // Só mostra "Bem-vindo" agora que ele pode ver

                    jogador.sendMessage(Component.text("Lenda carregada com sucesso!").color(NamedTextColor.GREEN));

                    Component titulo = Component.text("BEM-VINDO").color(NamedTextColor.GOLD);
                    Component subtitulo = Component.text("Sua jornada começa agora, " + dados.getClasse().getNome());

                    Title.Times tempos = Title.Times.times(Duration.ofMillis(500), // 0.5 segundos para aparecer
                            Duration.ofMillis(3000), // 3 segundos na tela
                            Duration.ofMillis(1000)); // 1 segundo para sumir
                    jogador.showTitle(Title.title(titulo, subtitulo, tempos));

                    // Som de sucesso
                    // (Beacon Activate é mais agradável que o dragão para login)
                    jogador.playSound(jogador.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);

                    // Cria o placar lateral
                    new GerentePlacar().criarPlacar(jogador);

                    plugin.getLogger().info("Jogador " + jogador.getName() + " liberado no mundo.");
                });
            }
        });
    }
}
