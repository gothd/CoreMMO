package br.com.ruasvivas.coreMMO.eventos;

import br.com.ruasvivas.coreMMO.CoreMMO;
import br.com.ruasvivas.coreMMO.menus.MenuClasses;
import br.com.ruasvivas.coreMMO.model.ClasseRPG;
import br.com.ruasvivas.coreMMO.model.DadosJogador;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MenuClassesListener implements Listener {

    private final CoreMMO plugin;

    public MenuClassesListener(CoreMMO plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void aoClicar(InventoryClickEvent event) {
        if (!event.getView().title().equals(MenuClasses.TITULO)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clicado = event.getCurrentItem();
        if (clicado == null || clicado.getType() == Material.AIR) return;

        // Identifica a classe pelo ícone
        ClasseRPG escolhida = null;
        for (ClasseRPG c : ClasseRPG.values()) {
            if (c.getIcone() == clicado.getType()) {
                escolhida = c;
                break;
            }
        }

        if (escolhida != null) {
            processarEscolha(player, escolhida);
        }
    }

    private void processarEscolha(Player jogador, ClasseRPG novaClasse) {
        DadosJogador dados = plugin.getGerenteDados().getDados(jogador.getUniqueId());

        if (dados.getClasse() != ClasseRPG.NOVATO) {
            jogador.sendMessage(
                    Component.text(
                            "Você já é um " + dados.getClasse().getNome() + "!"
                    ).color(NamedTextColor.RED));
            jogador.closeInventory();
            jogador.playSound(
                    jogador.getLocation(),
                    Sound.ENTITY_VILLAGER_NO, 1f, 1f
            );
            return;
        }

        dados.setClasse(novaClasse);
        jogador.closeInventory();

        jogador.sendMessage(Component.text("Destino selado! Você agora é um ")
                .color(NamedTextColor.GREEN)
                .append(Component.text(novaClasse.getNome())
                        .color(novaClasse.getCor())));

        jogador.playSound(
                jogador.getLocation(),
                Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f
        );

        // Salva Async
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getJogadorDAO().salvarJogador(dados);
        });
    }
}