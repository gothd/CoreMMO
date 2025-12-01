package br.com.ruasvivas.coreMMO;

import br.com.ruasvivas.coreMMO.comandos.ComandoEspada;
import org.bukkit.plugin.java.JavaPlugin;
import br.com.ruasvivas.coreMMO.comandos.ComandoCurar;

import java.util.Objects;

public final class CoreMMO extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Core Online.");

        // REGISTRO DE COMANDOS
        // "Ei servidor, o comando 'curar' será resolvido pela classe ComandoCurar"
        // Usamos Objects.requireNonNull para garantir segurança se o comando não existir no yml
        Objects.requireNonNull(getCommand("curar")).setExecutor(new ComandoCurar());
        Objects.requireNonNull(getCommand("espada")).setExecutor(new ComandoEspada());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
