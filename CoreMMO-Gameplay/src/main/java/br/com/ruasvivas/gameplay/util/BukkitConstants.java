package br.com.ruasvivas.gameplay.util;

import br.com.ruasvivas.common.util.GameConstants;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public class BukkitConstants {

    public static NamespacedKey RPG_ITEM_KEY;
    public static NamespacedKey RPG_ARMOR_KEY;

    public static void init(Plugin plugin) {
        // Converte as Strings do Common para Keys do Bukkit
        RPG_ITEM_KEY = new NamespacedKey(plugin, GameConstants.KEY_RPG_ITEM_DATA);
        RPG_ARMOR_KEY = new NamespacedKey(plugin, GameConstants.KEY_RPG_ARMOR_VALUE);
    }
}