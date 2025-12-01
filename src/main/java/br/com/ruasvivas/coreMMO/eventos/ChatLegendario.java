package br.com.ruasvivas.coreMMO.eventos;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatLegendario implements Listener {

    @EventHandler
    public void aoFalar(AsyncChatEvent evento) {
        // O renderizador define COMO a mensagem aparece para os outros
        evento.renderer((source, sourceDisplayName, message, viewer) -> {

            // Montando as peças (Componentes)
            Component nivel = Component.text("[Nv.1] ").color(NamedTextColor.GREEN);
            Component classe = Component.text("[Guerreiro] ").color(NamedTextColor.YELLOW);

            // Juntando tudo com .append() (Anexar)
            return nivel
                    .append(classe)
                    .append(sourceDisplayName)
                    .append(Component.text(": ").color(NamedTextColor.GRAY))
                    .append(message);
        });
    }
}
