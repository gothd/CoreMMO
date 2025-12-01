package br.com.ruasvivas.coreMMO;

import br.com.ruasvivas.coreMMO.comandos.ComandoEspada;
import br.com.ruasvivas.coreMMO.eventos.ChatLegendario;
import br.com.ruasvivas.coreMMO.eventos.EntradaJornada;
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

        // REGISTRO DE EVENTOS
        // "Servidor, pegue seu Gerente de Plugins e registre os eventos desta classe"
        // 'this' significa que o plugin dono é este aqui (CoreMMO).
        getServer().getPluginManager().registerEvents(new EntradaJornada(), this);
        getServer().getPluginManager().registerEvents(new ChatLegendario(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
