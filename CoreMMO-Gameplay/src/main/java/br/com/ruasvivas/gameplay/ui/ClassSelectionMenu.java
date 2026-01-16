package br.com.ruasvivas.gameplay.ui;

import br.com.ruasvivas.common.model.RPGClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ClassSelectionMenu {

    public static final Component TITLE = Component.text("Escolha seu Destino")
            .color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD);

    public static Inventory create() {
        Inventory inv = Bukkit.createInventory(null, 9, TITLE);

        // Mapeando as classes para slots específicos
        // No futuro, isso pode ser um loop dinâmico
        inv.setItem(2, createIcon(RPGClass.WARRIOR));
        inv.setItem(4, createIcon(RPGClass.MAGE));
        inv.setItem(6, createIcon(RPGClass.ARCHER));

        return inv;
    }

    private static ItemStack createIcon(RPGClass rpgClass) {
        // CONVERSÃO: String (Common) -> Material (Bukkit)
        Material material;
        try {
            material = Material.valueOf(rpgClass.getIconMaterial());
        } catch (IllegalArgumentException e) {
            material = Material.STONE; // Fallback se o nome estiver errado
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        // Limpeza visual
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        // CONVERSÃO: String Cor -> NamedTextColor
        NamedTextColor color = getColorByName(rpgClass.getColorName());

        // Nome Exibido (PT-BR)
        meta.displayName(Component.text(rpgClass.getDisplayName())
                .color(color)
                .decoration(TextDecoration.ITALIC, false)
                .decorate(TextDecoration.BOLD));

        // Descrição (Lore)
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        for (String line : rpgClass.getDescription()) {
            lore.add(Component.text(line)
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }

        lore.add(Component.empty());
        lore.add(Component.text("Clique para escolher!")
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        item.setItemMeta(meta);

        return item;
    }

    // Helper simples para converter nomes de cores comuns
    private static NamedTextColor getColorByName(String colorName) {
        return switch (colorName.toUpperCase()) {
            case "RED" -> NamedTextColor.RED;
            case "BLUE" -> NamedTextColor.BLUE;
            case "GREEN" -> NamedTextColor.GREEN;
            case "YELLOW" -> NamedTextColor.YELLOW;
            case "GOLD" -> NamedTextColor.GOLD;
            default -> NamedTextColor.GRAY;
        };
    }
}