package br.com.ruasvivas.coreMMO.eventos;

import br.com.ruasvivas.coreMMO.CoreMMO;
import br.com.ruasvivas.coreMMO.model.DadosJogador;
import br.com.ruasvivas.coreMMO.model.Guilda;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class CanalChatListener implements Listener {

    private final CoreMMO plugin;
    private final int RAIO_LOCAL = 50;
    // Otimização: Guardamos o raio ao quadrado para evitar Math.sqrt()
    private final double RAIO_QUADRADO = RAIO_LOCAL * RAIO_LOCAL;

    public CanalChatListener(CoreMMO plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void aoFalar(AsyncChatEvent event) {
        Player jogador = event.getPlayer();
        // Converte Component para String para checar o prefixo
        String textoOriginal = PlainTextComponentSerializer.plainText()
                .serialize(event.message());

        boolean ehGlobal = false;
        boolean ninguemOuviu = false;

        // --- 1. LÓGICA DE CANAIS (Roteamento) ---

        if (textoOriginal.startsWith("!")) {
            // MODO GLOBAL
            ehGlobal = true;
            String textoFinal = textoOriginal.substring(1).trim();

            if (textoFinal.isEmpty()) {
                event.setCancelled(true); // Evita mensagens vazias "! "
                return;
            }
            // Atualiza a mensagem removendo o '!'
            event.message(Component.text(textoFinal));

        } else {
            // MODO LOCAL

            // O Filtro: Remove quem está longe ou em outro mundo
            event.viewers().removeIf(viewer -> {
                if (!(viewer instanceof Player destinatario)) return false;

                // Você sempre se ouve
                if (destinatario.getUniqueId().equals(jogador.getUniqueId()))
                    return false;

                if (!destinatario.getWorld().equals(jogador.getWorld()))
                    return true;

                // Pitágoras Otimizado
                return destinatario.getLocation()
                        .distanceSquared(jogador.getLocation()) > RAIO_QUADRADO;
            });

            // Verificação de "Solidão"
            // Usamos .noneMatch() que age como um loop com 'break'.
            // Assim que ele encontra 1 outro jogador, ele para e retorna false.
            boolean ninguemMaisOuviu = event.viewers().stream()
                    .filter(v -> v instanceof Player) // Ignora Console
                    .noneMatch(v -> !((Player)v).getUniqueId()
                            .equals(jogador.getUniqueId()));

            if (ninguemMaisOuviu) {
                ninguemOuviu = true;
            }
        }

        // --- 2. LÓGICA DE DESIGN (Renderização) ---

        // Precisamos de variáveis "final" ou efetivamente final para o lambda
        boolean finalEhGlobal = ehGlobal;
        boolean finalNinguemOuviu = ninguemOuviu;

        // Define como a mensagem será desenhada para quem receber
        event.renderer(new ChatRenderer() {
            @Override
            public @NotNull Component render(
                    @NotNull Player source,
                    @NotNull Component sourceDisplayName,
                    @NotNull Component message,
                    @NotNull Audience viewer
            ) {
                return formatarMensagem(source, message,
                        finalEhGlobal, finalNinguemOuviu);
            }
        });
    }

    // O "Artista": Método auxiliar que monta o visual da mensagem
    private Component formatarMensagem(Player jogador, Component mensagem,
                                       boolean global, boolean ninguemOuviu) {

        DadosJogador dados = plugin.getGerenteDados()
                .getDados(jogador.getUniqueId());

        NamedTextColor corTag;
        NamedTextColor corTexto;
        String tagCanal;

        // Definição de Paleta de Cores
        if (global) {
            tagCanal = "[G]";
            corTag = NamedTextColor.DARK_GREEN;
            corTexto = NamedTextColor.WHITE;
        } else if (ninguemOuviu) {
            tagCanal = "[?]"; // Modo Fantasma 👻
            corTag = NamedTextColor.DARK_GRAY;
            corTexto = NamedTextColor.DARK_GRAY;
        } else {
            tagCanal = "[L]";
            corTag = NamedTextColor.YELLOW;
            corTexto = NamedTextColor.GRAY;
        }

        // Montagem: [Tag] [Lvl] [Guilda] Nick: Msg
        Component prefixo = Component.text(tagCanal + " ").color(corTag);

        if (ninguemOuviu) {
            prefixo = prefixo.decorate(TextDecoration.ITALIC);
        }

        if (dados != null) {
            prefixo = prefixo.append(Component.text("[" + dados.getNivel() + "] ")
                    .color(NamedTextColor.YELLOW));

            if (dados.temGuilda()) {
                Guilda guilda = plugin.getGerenteGuilda()
                        .getPorId(dados.getGuildaId());
                if (guilda != null) {
                    prefixo = prefixo.append(Component.text(guilda.getTag() + " ")
                            .color(NamedTextColor.AQUA));
                }
            }
        }

        Component corpoMensagem = mensagem.color(corTexto);
        if (ninguemOuviu) corpoMensagem = corpoMensagem.decorate(TextDecoration.ITALIC);

        return prefixo
                .append(jogador.displayName().color(NamedTextColor.WHITE))
                .append(Component.text(": ").color(NamedTextColor.GRAY))
                .append(corpoMensagem);
    }
}