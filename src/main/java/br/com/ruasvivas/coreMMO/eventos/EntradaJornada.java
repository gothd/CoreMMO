package br.com.ruasvivas.coreMMO.eventos;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent; // O Evento de Entrar
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

import java.time.Duration;

public class EntradaJornada implements Listener {

    // @EventHandler é OBRIGATÓRIO. Sem isso, o servidor ignora o código.
    @EventHandler
    public void aoEntrar(PlayerJoinEvent evento) {
        Player jogador = evento.getPlayer();

        // 1. Mensagem no Chat
        jogador.sendMessage(Component.text("Bem-vindo à sua lenda!")
                .color(NamedTextColor.AQUA));

        // 2. Título na Tela (Recurso visual forte)
        Component titulo = Component.text("BEM-VINDO").color(NamedTextColor.GOLD);
        Component subtitulo = Component.text("Prepare-se, " + jogador.getName());

        // Definindo a duração (Aparece, Fica, Some)
        Title.Times tempos = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofMillis(3000),
                Duration.ofMillis(1000)
        );

        Title tituloFinal = Title.title(titulo, subtitulo, tempos);
        jogador.showTitle(tituloFinal);

        // 3. Efeito Sonoro (Sound Design)
        // Toca o som na posição do jogador
        // 1.0f = Volume Máximo, 1.0f = Velocidade Normal
        jogador.playSound(jogador.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
    }
}
