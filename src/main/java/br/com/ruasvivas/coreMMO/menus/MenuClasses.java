package br.com.ruasvivas.coreMMO.menus;

import br.com.ruasvivas.coreMMO.model.ClasseRPG;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MenuClasses {

    public static final Component TITULO = Component.text("Escolha seu Destino")
            .color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD);

    public static Inventory criar() {
        Inventory inv = Bukkit.createInventory(null, 9, TITULO);

        inv.setItem(2, criarIcone(ClasseRPG.GUERREIRO));
        inv.setItem(4, criarIcone(ClasseRPG.MAGO));
        inv.setItem(6, criarIcone(ClasseRPG.ARQUEIRO));

        return inv;
    }

    private static ItemStack criarIcone(ClasseRPG classe) {
        ItemStack item = new ItemStack(classe.getIcone());
        ItemMeta meta = item.getItemMeta();

        // Remove texto de atributos (+7 Damage) para ficar limpo
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        meta.displayName(Component.text(classe.getNome())
                .color(classe.getCor())
                .decoration(TextDecoration.ITALIC, false)
                .decorate(TextDecoration.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        for (String linha : classe.getDescricao()) {
            lore.add(Component.text(linha)
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
}