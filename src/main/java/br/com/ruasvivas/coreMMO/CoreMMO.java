package br.com.ruasvivas.coreMMO;

import br.com.ruasvivas.coreMMO.banco.GerenteBanco;
import br.com.ruasvivas.coreMMO.cache.GerenteCooldowns;
import br.com.ruasvivas.coreMMO.cache.GerenteDados;
import br.com.ruasvivas.coreMMO.cache.GerenteGuilda;
import br.com.ruasvivas.coreMMO.comandos.*;
import br.com.ruasvivas.coreMMO.dao.GuildaDAO;
import br.com.ruasvivas.coreMMO.dao.JogadorDAO;
import br.com.ruasvivas.coreMMO.economia.GerenteEconomia;
import br.com.ruasvivas.coreMMO.eventos.BatalhaListener;
import br.com.ruasvivas.coreMMO.eventos.EntradaJornada;
import br.com.ruasvivas.coreMMO.eventos.SaidaJornada;
import br.com.ruasvivas.coreMMO.menus.MenuClasses;
import br.com.ruasvivas.coreMMO.model.Guilda;
import br.com.ruasvivas.coreMMO.npcs.GerenteNPC;
import br.com.ruasvivas.coreMMO.npcs.NPCListener;
import br.com.ruasvivas.coreMMO.tasks.AutoSaveTask;
import br.com.ruasvivas.coreMMO.tasks.RegeneracaoTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public final class CoreMMO extends JavaPlugin {

    private GerenteBanco gerenteBanco;
    private GerenteDados gerenteDados;
    private GerenteCooldowns gerenteCooldowns;
    private GerenteEconomia gerenteEconomia;
    private JogadorDAO jogadorDAO;
    private GerenteGuilda gerenteGuilda;
    private GuildaDAO guildaDAO;
    private GerenteNPC gerenteNPC;

    @Override
    public void onEnable() {
        // 1. Salva o config.yml se ele não existir na pasta do servidor
        saveDefaultConfig();

        // Inicializa o cache (memória apenas, é rápido)
        gerenteDados = new GerenteDados();
        gerenteCooldowns = new GerenteCooldowns();
        gerenteGuilda = new GerenteGuilda();
        gerenteEconomia = new GerenteEconomia(this);
        gerenteNPC = new GerenteNPC(this);

        // 2. Inicializa o banco
        gerenteBanco = new GerenteBanco(this);

        try {
            gerenteBanco.abrirConexao();

            // Inicializa DAOs
            jogadorDAO = new JogadorDAO(this, gerenteBanco);
            guildaDAO = new GuildaDAO(this, gerenteBanco);

            // 3. ORQUESTRAÇÃO (Load Inicial)
            // Tira do Disco (DAO) -> Põe na RAM (Gerente)
            getLogger().info("Carregando guildas...");
            for (Guilda g : guildaDAO.carregarTodas()) {
                gerenteGuilda.registrarGuilda(g);
            }
            getLogger().info("Guildas carregadas!");

        } catch (Exception e) {
            // Log robusto com a exceção completa
            getLogger().log(Level.SEVERE, "Falha crítica ao iniciar banco de dados!", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // REGISTRO DE COMANDOS
        // "Ei servidor, o comando 'curar' será resolvido pela classe ComandoCurar"
        // Usamos Objects.requireNonNull para garantir segurança se o comando não existir no yml
        Objects.requireNonNull(getCommand("curar")).setExecutor(new ComandoCurar());
        Objects.requireNonNull(getCommand("espada")).setExecutor(new ComandoEspada());
        // Registrando Economia
        Objects.requireNonNull(getCommand("saldo")).setExecutor(new ComandoSaldo(this));
        Objects.requireNonNull(getCommand("pagar")).setExecutor(new ComandoPagar(this));
        // Registrando Guilda
        Objects.requireNonNull(getCommand("guilda")).setExecutor(new ComandoGuilda(this));

        // REGISTRO DE EVENTOS
        // "Servidor, pegue seu Gerente de Plugins e registre os eventos desta classe"
        // 'this' significa que o plugin dono é este aqui (CoreMMO).
        getServer().getPluginManager().registerEvents(new EntradaJornada(this), this);
        getServer().getPluginManager().registerEvents(
                new br.com.ruasvivas.coreMMO.eventos.CanalChatListener(this), this
        );
        getServer().getPluginManager().registerEvents(new SaidaJornada(this), this);
        // Registra os eventos de batalha
        getServer().getPluginManager().registerEvents(new BatalhaListener(this), this);
        // Registra NPCs
        getServer().getPluginManager().registerEvents(new NPCListener(this), this);

        // Instanciamos a classe uma vez
        MenuClasses menu = new MenuClasses();

        // 1. Registra o comando /classe
        Objects.requireNonNull(getCommand("classe")).setExecutor(menu);

        // 2. Registra o evento de clique
        getServer().getPluginManager().registerEvents(menu, this);

        // INICIANDO AS TASKS
        // runTaskTimer(plugin, delay, periodo)
        // Delay 0 (começa já), Repete a cada 12000 ticks (10 minutos)
        new AutoSaveTask(this).runTaskTimer(this, 0L, 12000L);
        // Inicia o relógio de regeneração (Delay 0, Repete a cada 20 ticks/1s)
        new RegeneracaoTask(this).runTaskTimer(this, 0L, 20L);

        // Delay de 1 tick para garantir que o mundo carregou antes de spawnar
        getServer().getScheduler().runTask(this, () -> {
            gerenteNPC.carregarNPCs();
        });

        getLogger().info("Core Online.");
    }

    @Override
    public void onDisable() {
        if (gerenteNPC != null) {
            gerenteNPC.desligarTudo();
        }
        if (gerenteBanco != null) {
            gerenteBanco.fecharConexao();
        }
    }

    // Getter útil para outras classes acessarem o banco
    public GerenteBanco getGerenteBanco() {
        return gerenteBanco;
    }

    public GerenteGuilda getGerenteGuilda() {
        return gerenteGuilda;
    }

    public GuildaDAO getGuildaDAO() {
        return guildaDAO;
    }

    // Getter para acesso externo
    public JogadorDAO getJogadorDAO() {
        return jogadorDAO;
    }

    public GerenteDados getGerenteDados() {
        return gerenteDados;
    }

    public GerenteCooldowns getGerenteCooldowns() {
        return gerenteCooldowns;
    }

    public GerenteEconomia getGerenteEconomia() {
        return gerenteEconomia;
    }
}
