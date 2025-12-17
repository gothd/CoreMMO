package br.com.ruasvivas.coreMMO.eventos;

import br.com.ruasvivas.coreMMO.CoreMMO;
import br.com.ruasvivas.coreMMO.habilidades.Habilidade;
import br.com.ruasvivas.coreMMO.model.ClasseRPG;
import br.com.ruasvivas.coreMMO.model.DadosJogador;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HabilidadeListener implements Listener {

    private final CoreMMO plugin;
    // Cache local para limitar sons de erro (1x por segundo)
    private final Map<UUID, Long> antiSpamAviso = new HashMap<>();

    public HabilidadeListener(CoreMMO plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void aoInteragir(PlayerInteractEvent event) {
        // 1. Filtros Básicos (Mão Principal e Clique Direito)
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        DadosJogador dados = plugin.getGerenteDados().getDados(player.getUniqueId());

        // Se for Novato, não faz nada (deixa o comportamento vanilla)
        if (dados.getClasse() == ClasseRPG.NOVATO) return;

        // 2. Verifica Item na Mão
        Material itemMao = player.getInventory().getItemInMainHand().getType();
        if (itemMao != dados.getClasse().getIcone()) return;

        // 3. CORREÇÃO DE INPUT: Cancela o evento nativo
        // Isso impede o Arco de puxar corda e a Espada de defender/bloquear.
        event.setCancelled(true);

        // 4. Busca Habilidade
        Habilidade skill = plugin.getGerenteHabilidades().getHabilidade(dados.getClasse());
        if (skill == null) return;

        // 5. Verifica Cooldown (Usa GerenteCooldowns do cache)
        if (plugin.getGerenteCooldowns().emCooldown(player.getUniqueId(), skill.getNome())) {
            if (podeEnviarAviso(player)) {
                double falta = plugin.getGerenteCooldowns().getSegundosRestantes(player.getUniqueId(), skill.getNome());

                Component msg =
                        Component.text("Recarregando: " + String.format("%.1f", falta) + "s").color(NamedTextColor.RED);

                // Nível 2: Aviso com Bloqueio
                plugin.getGerenteDados().enviarAviso(player, msg);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            }
            return;
        }

        // 6. Verifica Mana
        if (dados.getMana() < skill.getCustoMana()) {
            if (podeEnviarAviso(player)) {
                Component msg = Component.text("Mana insuficiente!").color(NamedTextColor.BLUE);
                plugin.getGerenteDados().enviarAviso(player, msg);
                player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 2f);
            }
            return;
        }

        // 7. Executa a Habilidade
        boolean sucesso = skill.usar(player);

        if (sucesso) {
            // Consome Recursos
            dados.setMana(dados.getMana() - skill.getCustoMana());

            plugin.getGerenteCooldowns().adicionarCooldown(player.getUniqueId(), skill.getNome(),
                    skill.getCooldownSegundos());

            // Nível 3: Força atualização imediata (para ver a mana descer)
            plugin.getGerenteDados().forcarAtualizacao(player);
        }
    }

    // Regra do Anti-Spam
    // (retorna true apenas se passou 1 segundo desde o último aviso)
    private boolean podeEnviarAviso(Player player) {
        long agora = System.currentTimeMillis();
        long ultimo = antiSpamAviso.getOrDefault(player.getUniqueId(), 0L);
        if (agora - ultimo > 1000) {
            antiSpamAviso.put(player.getUniqueId(), agora);
            return true;
        }
        return false;
    }
}