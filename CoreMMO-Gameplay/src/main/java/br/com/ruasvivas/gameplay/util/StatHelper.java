package br.com.ruasvivas.gameplay.util;

import br.com.ruasvivas.common.model.User;
import br.com.ruasvivas.common.util.GameConstants;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class StatHelper {

    /**
     * Sincroniza os status do User (Common) com o Player (Bukkit).
     * Recalcula MaxMana, MaxHealth e ajusta a escala visual.
     */
    public static void syncStats(Player player, User user) {
        // Recalcula a matemática interna (Mana Max e Lógica de HP)
        user.recalculateStats();

        // Aplica Vida Máxima Real no Bukkit (Lógica do Servidor)
        double newMaxHealth = user.getCalculatedMaxHealth();
        AttributeInstance healthAttr = player.getAttribute(Attribute.MAX_HEALTH);

        if (healthAttr != null) {
            // Só aplica se houve mudança para economizar processamento
            if (healthAttr.getBaseValue() != newMaxHealth) {
                healthAttr.setBaseValue(newMaxHealth);
            }
        }

        // Aplica Escala Visual Fixa (Lógica do Cliente)
        // Trava a visualização em 20 Corações (40 pontos)
        // Assim, 1 Coração Visual = (MaxHealth / 20) de Vida Real
        if (player.isHealthScaled()) {
            // Se já estiver escalado, verifica se precisa redefinir (segurança)
            if (player.getHealthScale() != 40.0) player.setHealthScale(40.0);
        } else {
            player.setHealthScale(40.0);
            player.setHealthScaled(true);
        }

        // Ajusta velocidade de caminhada (Exemplo para o futuro: Stamina/Agilidade)
        // if (user.getRpgClass() == RPGClass.ARCHER) player.setWalkSpeed(0.25f);

        // Atualização da barra de armadura (Visual Proporcional)
        updateVisualArmor(player);
    }

    /**
     * Lê o NBT dos itens, soma a defesa real e converte para a barra visual vanilla.
     */
    public static void updateVisualArmor(Player player) {
        double totalRpgArmor = getPlayerTotalArmor(player);

        // Regra de Três:
        // MAX_VISUAL_ARMOR_CAP (ex: 400) --- 20 pontos (Barra cheia)
        // totalRpgArmor                  --- X pontos

        double visualValue = (totalRpgArmor / GameConstants.MAX_VISUAL_ARMOR_CAP) * 20.0;

        // Trava entre 0 e 20 para não bugar o visual
        visualValue = Math.min(20.0, Math.max(0.0, visualValue));

        // Aplica no atributo vanilla APENAS para visualização
        AttributeInstance armorAttr = player.getAttribute(Attribute.ARMOR);
        if (armorAttr != null) {
            armorAttr.setBaseValue(visualValue);
        }
    }

    /**
     * Calcula a defesa RPG real baseada no NBT.
     */
    public static double getPlayerTotalArmor(Player player) {
        double total = 0.0;
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.hasItemMeta()) {
                Double value = item.getItemMeta().getPersistentDataContainer()
                        .get(BukkitConstants.RPG_ARMOR_KEY, PersistentDataType.DOUBLE);
                if (value != null) {
                    total += value;
                }
            }
        }
        return total;
    }
}