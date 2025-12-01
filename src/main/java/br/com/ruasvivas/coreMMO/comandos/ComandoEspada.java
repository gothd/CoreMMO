package br.com.ruasvivas.coreMMO.comandos;

// Imports essenciais para atributos na versão 1.21

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ComandoEspada implements CommandExecutor {

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (sender instanceof Player jogador) {

            // 1. Criando o Item
            ItemStack espada = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta meta = espada.getItemMeta();

            // 2. Cosmética (Nome e Lore)
            meta.displayName(Component.text("Excalibur").color(NamedTextColor.GOLD));
            meta.lore(List.of(
                    Component.text("A espada dos reis."),
                    Component.text("Dano: Infinito").color(NamedTextColor.RED)
            ));

            // 3. ENGENHARIA: Adicionando Dano Real (+9999)
            // Criamos uma chave única (namespace) para o atributo
            NamespacedKey chave = new NamespacedKey("coremmo", "dano_infinito");

            AttributeModifier danoAbsurdo = new AttributeModifier(
                    chave,
                    9999.0,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.HAND // Funciona na mão principal e secundária
            );

            // Injetamos o modificador no atributo de Dano Genérico
            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, danoAbsurdo);

            // 4. Entregando
            espada.setItemMeta(meta);
            jogador.getInventory().addItem(espada);
            jogador.sendMessage(Component.text("Você recebeu o poder absoluto!").color(NamedTextColor.YELLOW));

            return true;
        }
        return false;
    }
}