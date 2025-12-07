package br.com.ruasvivas.coreMMO.comandos;

import br.com.ruasvivas.coreMMO.CoreMMO;
import br.com.ruasvivas.coreMMO.model.DadosJogador;
import br.com.ruasvivas.coreMMO.model.Guilda;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ComandoGuilda implements CommandExecutor {

    private final CoreMMO plugin;
    // Mapa temporário para confirmação de deletação
    private final Map<UUID, Long> confirmacaoDeletar = new HashMap<>();

    public ComandoGuilda(CoreMMO plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command cmd,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (!(sender instanceof Player jogador)) return false;

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("criar")) {
                handleCriar(jogador, args);
                return true;
            }
            if (args[0].equalsIgnoreCase("info")) {
                handleInfo(jogador, args);
                return true;
            }
            if (args[0].equalsIgnoreCase("deletar")) {
                handleDeletar(jogador);
                return true;
            }
        }

        jogador.sendMessage(Component.text("Use: /guilda <criar|info|deletar>")
                .color(NamedTextColor.YELLOW));
        return true;
    }

    private void handleCriar(Player jogador, String[] args) {
        if (args.length < 3) {
            jogador.sendMessage(Component.text("/guilda criar <tag> <nome>")
                    .color(NamedTextColor.RED));
            return;
        }

        String tag = args[1];
        String nome = unirArgumentos(args, 2);
        long custo = 5000;

        DadosJogador dados = plugin.getGerenteDados()
                .getDados(jogador.getUniqueId());

        if (dados.temGuilda()) {
            jogador.sendMessage(Component.text("Você já tem guilda!")
                    .color(NamedTextColor.RED));
            return;
        }

        if (dados.getMoedas() < custo) {
            jogador.sendMessage(Component.text("Custa " + custo + " moedas.")
                    .color(NamedTextColor.RED));
            return;
        }

        // Criando (Async)
        Guilda nova = new Guilda(nome, tag, jogador.getUniqueId());
        dados.setMoedas(dados.getMoedas() - custo);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getGuildaDAO().criar(nova);

            // Volta para Sync para atualizar cache
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (nova.getId() > 0) {
                    plugin.getGerenteGuilda().registrarGuilda(nova);
                    dados.setGuildaId(nova.getId());

                    // Salva jogador atualizado no banco imediatamente
                    plugin.getServer().getScheduler()
                            .runTaskAsynchronously(plugin, () -> {
                                plugin.getJogadorDAO().salvarJogador(dados);
                            });

                    jogador.sendMessage(Component.text("Guilda criada!")
                            .color(NamedTextColor.GREEN));
                }
            });
        });
    }

    private void handleInfo(Player jogador, String[] args) {
        String busca = (args.length > 1) ? unirArgumentos(args, 1) : "";
        Guilda alvo = plugin.getGerenteGuilda().getPorNome(busca);

        if (alvo == null) {
            jogador.sendMessage(Component.text("Guilda não encontrada.")
                    .color(NamedTextColor.RED));
            return;
        }

        jogador.sendMessage(Component.text("Carregando...")
                .color(NamedTextColor.GRAY));

        // Busca estatísticas no banco (Async)
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getGuildaDAO().carregarEstatisticas(alvo);

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                jogador.sendMessage(Component.text("Guilda: " + alvo.getNome())
                        .color(NamedTextColor.GOLD));
                jogador.sendMessage(Component.text("KDR: " +
                                String.format("%.2f", alvo.getKDR()))
                        .color(NamedTextColor.YELLOW));
                jogador.sendMessage(Component.text("Membros: " +
                                alvo.getQuantidadeMembros() + "/" + alvo.getMembrosMax())
                        .color(NamedTextColor.AQUA));
            });
        });
    }

    private void handleDeletar(Player jogador) {
        DadosJogador dados = plugin.getGerenteDados()
                .getDados(jogador.getUniqueId());
        Guilda guilda = plugin.getGerenteGuilda()
                .getPorId(dados.getGuildaId());

        if (guilda == null || !guilda.getLiderUuid().equals(jogador.getUniqueId())) {
            jogador.sendMessage(Component.text("Apenas o líder pode deletar.")
                    .color(NamedTextColor.RED));
            return;
        }

        UUID id = jogador.getUniqueId();
        if (!confirmacaoDeletar.containsKey(id)) {
            confirmacaoDeletar.put(id, System.currentTimeMillis());
            jogador.sendMessage(Component.text("⚠️ Confirme digitando novamente.")
                    .color(NamedTextColor.RED));
            return;
        }
        confirmacaoDeletar.remove(id);

        int idGuilda = guilda.getId();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            // 1. Apaga do banco (Cascade set NULL)
            plugin.getGuildaDAO().deletar(idGuilda);

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                // 2. Apaga do cache
                plugin.getGerenteGuilda().removerGuilda(guilda);

                // 3. Atualiza jogadores online
                for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                    var d = plugin.getGerenteDados().getDados(p.getUniqueId());
                    if (d != null && d.getGuildaId() == idGuilda) {
                        d.setGuildaId(0);
                        p.sendMessage(Component.text("Guilda dissolvida!")
                                .color(NamedTextColor.RED));
                    }
                }
            });
        });
    }

    private String unirArgumentos(String[] args, int inicio) {
        StringBuilder sb = new StringBuilder();
        for (int i = inicio; i < args.length; i++) {
            if (i > inicio) sb.append(" ");
            sb.append(args[i]);
        }
        return sb.toString();
    }
}