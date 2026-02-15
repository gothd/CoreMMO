package br.com.ruasvivas.gameplay.listener;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class RegenListener implements Listener {

    @EventHandler
    public void onRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        double amount = event.getAmount();
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) return;

        // Lógica de Escalonamento
        // O valor padrão de referência do Minecraft é 20 HP (10 corações).
        // Se a cura for 4 (Bife), isso representa 20% de 20 HP.
        // Queremos que cure 20% da vida do Guerreiro (500 HP) também.

        // Fator: (CuraOriginal / 20.0)
        double percentage = amount / 20.0;

        // Nova Cura: Porcentagem * VidaMaximaReal
        double newAmount = percentage * maxHealth.getValue();

        event.setAmount(newAmount);
    }
}