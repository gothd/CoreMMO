package br.com.ruasvivas.gameplay.util;

import org.bukkit.Material;
import org.bukkit.inventory.PlayerInventory;

public class InventoryUtil {

    /**
     * Verifica se o material é uma peça de armadura (Capacete, Peito, Calça ou Bota).
     */
    public static boolean isArmor(Material mat) {
        if (mat == null) return false;
        String name = mat.name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") ||
                name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
    }

    /**
     * Verifica se o slot de equipamento correspondente ao material está vazio.
     * Útil para prever se um Shift-Click vai equipar o item ou apenas mover para o inventário.
     */
    public static boolean isSlotEmptyFor(PlayerInventory inv, Material mat) {
        if (mat == null) return false;
        String name = mat.name();
        if (name.endsWith("_HELMET")) return inv.getHelmet() == null;
        if (name.endsWith("_CHESTPLATE")) return inv.getChestplate() == null;
        if (name.endsWith("_LEGGINGS")) return inv.getLeggings() == null;
        if (name.endsWith("_BOOTS")) return inv.getBoots() == null;
        return false;
    }
}