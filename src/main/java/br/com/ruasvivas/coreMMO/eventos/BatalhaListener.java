package br.com.ruasvivas.coreMMO.eventos;

import br.com.ruasvivas.coreMMO.CoreMMO;
import br.com.ruasvivas.coreMMO.model.DadosJogador;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot; // Importante!

import java.util.UUID;

public class BatalhaListener implements Listener {

    private final CoreMMO plugin;

    public BatalhaListener(CoreMMO plugin) {
        this.plugin = plugin;
    }

    // EVENTO 1: Atualizar barra ao tomar dano
    @EventHandler
    public void aoTomarDano(EntityDamageEvent evento) {
        if (evento.getEntity() instanceof Player jogador) {
            DadosJogador dados = plugin.getGerenteDados().getDados(jogador.getUniqueId());

            if (dados != null) {
                // Pequeno delay (1 tick) para o Bukkit processar o dano
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    plugin.getGerenteDados().atualizarBarra(jogador, dados);
                }, 1L);
            }
        }
    }

    // EVENTO 2: Gastar mana com magia (Blaze Rod)
    @EventHandler
    public void aoUsarMagia(PlayerInteractEvent evento) {
        // Evita clique duplo (Mão Principal apenas)
        if (evento.getHand() != EquipmentSlot.HAND) return;

        if (evento.getAction() == Action.RIGHT_CLICK_AIR || evento.getAction() == Action.RIGHT_CLICK_BLOCK) {

            Player jogador = evento.getPlayer();

            // Item de teste
            if (jogador.getInventory().getItemInMainHand().getType() == Material.BLAZE_ROD) {

                // --- 1. VERIFICAÇÃO DE COOLDOWN ---
                UUID id = jogador.getUniqueId();
                String habilidade = "bola_fogo";

                if (plugin.getGerenteCooldowns().emCooldown(id, habilidade)) {
                    double falta = plugin.getGerenteCooldowns().getSegundosRestantes(id, habilidade);

                    jogador.sendMessage(Component.text("Aguarde " + String.format("%.1f", falta) + "s!")
                            .color(NamedTextColor.RED));
                    return; // Cancela a magia
                }
                // ----------------------------------

                DadosJogador dados = plugin.getGerenteDados().getDados(id);

                if (dados != null) {
                    double custo = 20.0;

                    if (dados.getMana() >= custo) {
                        // --- 2. APLICAÇÃO DO COOLDOWN ---
                        // Define 2 segundos de recarga se o uso for bem sucedido
                        plugin.getGerenteCooldowns().adicionarCooldown(id, habilidade, 2);

                        // GASTA E ATUALIZA
                        dados.setMana(dados.getMana() - custo);

                        jogador.launchProjectile(Fireball.class);
                        jogador.sendMessage(Component.text("Bola de Fogo!").color(NamedTextColor.GOLD));

                        plugin.getGerenteDados().atualizarBarra(jogador, dados);

                    } else {
                        jogador.sendMessage(Component.text("Mana insuficiente!").color(NamedTextColor.RED));
                    }
                }
            }
        }
    }
}