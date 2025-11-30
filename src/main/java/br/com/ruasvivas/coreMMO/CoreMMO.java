package br.com.ruasvivas.coreMMO;

import org.bukkit.plugin.java.JavaPlugin;

public final class CoreMMO extends JavaPlugin {

    @Override
    public void onEnable() {
        // Essa mensagem aparecerá no console preto quando o servidor ligar
        getLogger().info("O sistema MMORPG foi iniciado com sucesso!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
