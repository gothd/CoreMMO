package br.com.ruasvivas.gameplay.listener;

import br.com.ruasvivas.gameplay.manager.CacheManager;
import br.com.ruasvivas.gameplay.util.StatHelper;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class CombatListener implements Listener {

    private final JavaPlugin plugin;
    private final CacheManager cacheManager;

    public CombatListener(JavaPlugin plugin, CacheManager cacheManager) {
        this.plugin = plugin;
        this.cacheManager = cacheManager;
    }

    // Prioridade HIGH: Rodamos antes de plugins de proteção final, mas depois de plugins básicos
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCombatDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Ignora danos que armadura não deve proteger
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID ||
                event.getCause() == EntityDamageEvent.DamageCause.SUICIDE ||
                event.getCause() == EntityDamageEvent.DamageCause.STARVATION) {
            return;
        }

        // Obtém o Dano Bruto (Raw)
        // 'event.getDamage()' retorna o dano base antes dos modificadores de armadura serem aplicados pelo servidor
        double rawDamage = event.getDamage();

        // Calcula a Redução RPG (Baseada no NBT Invisível)
        double rpgArmor = StatHelper.getPlayerTotalArmor(player);

        // Fórmula RPG: DanoFinal = Dano * (100 / (100 + ArmaduraRPG))
        // Ex: 100 Armor = 50% de Dano (Fator 0.5)
        double reductionFactor = 100.0 / (100.0 + rpgArmor);
        double targetDamage = rawDamage * reductionFactor;

        // Compensação da Armadura Visual Vanilla
        AttributeInstance vanillaArmor = player.getAttribute(Attribute.ARMOR);

        // Se tiver armadura visual, precisamos inflar o dano
        if (vanillaArmor != null) {
            // É preciso saber quanto o Minecraft VAI reduzir para 'enganar' o sistema.
            double vanillaFactor = getVanillaFactor(vanillaArmor);

            // Infla o dano: Se queremos dar 10 de dano e o vanilla reduz pra 20%, enviamos 50.
            targetDamage = targetDamage / vanillaFactor;
        }

        // Debug (Opcional - remova em produção)
        //player.sendMessage("§7[Debug] Dano: " + String.format("%.1f", rawDamage) +
        //        " -> " + String.format("%.1f", targetDamage) + " (Def: " + (int) rpgArmor + ")");

        // Aplica o Dano Calculado
        // O servidor vai pegar esse valor, aplicar a armadura visual, e chegar no resultado da fórmula RPG.
        event.setDamage(targetDamage);
    }

    private static double getVanillaFactor(AttributeInstance vanillaArmor) {
        double vanillaArmorPoints = vanillaArmor.getValue();
        // Fórmula Simplificada do Minecraft (aprox. 4% de redução por ponto de armadura)
        // FatorVanilla = 1 - (Pontos * 0.04)
        // Ex: 20 Pontos = 1 - 0.8 = 0.2 (Sobra 20% do dano)
        double vanillaFactor = 1.0 - (vanillaArmorPoints * 0.04);

        // Proteção contra divisão por zero ou números negativos (caso algo bugue e dê > 25 de armadura)
        if (vanillaFactor < 0.2) vanillaFactor = 0.2; // Limite de segurança (sempre passa 20%)
        return vanillaFactor;
    }

    // Usa MONITOR para ler o resultado final após cálculos de armadura
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {

            // Agendamento: Espera 1 tick para o Bukkit aplicar o dano na vida
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // Força atualização visual (Prioridade Máxima)
                cacheManager.forceUpdate(player);
            }, 1L);
        }
    }
}