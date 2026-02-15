package br.com.ruasvivas.gameplay;

import br.com.ruasvivas.api.CoreRegistry;
import br.com.ruasvivas.api.dao.GuildDAO;
import br.com.ruasvivas.api.dao.UserDAO;
import br.com.ruasvivas.api.database.IDatabase;
import br.com.ruasvivas.api.database.ITableManager;
import br.com.ruasvivas.api.service.CacheService;
import br.com.ruasvivas.api.service.EconomyService;
import br.com.ruasvivas.api.service.MobService;
import br.com.ruasvivas.gameplay.command.*;
import br.com.ruasvivas.gameplay.listener.*;
import br.com.ruasvivas.gameplay.manager.*;
import br.com.ruasvivas.gameplay.task.AutoSaveTask;
import br.com.ruasvivas.gameplay.task.RegenTask;
import br.com.ruasvivas.gameplay.ui.ScoreboardManager;
import br.com.ruasvivas.gameplay.util.BukkitConstants;
import br.com.ruasvivas.infra.dao.MariaDBGuildDAO;
import br.com.ruasvivas.infra.dao.MariaDBUserDAO;
import br.com.ruasvivas.infra.database.HikariDatabaseProvider;
import br.com.ruasvivas.infra.database.MariaDBTableManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public final class CorePlugin extends JavaPlugin {

    private static CorePlugin instance;
    private CacheManager cacheManager;
    private EconomyManager economyManager;
    private GuildManager guildManager;
    private CooldownManager cooldownManager;
    private SkillManager skillManager;
    private MobManager mobManager;
    private ScoreboardManager scoreboardManager;
    private LootManager lootManager;
    private DamageTrackerManager damageTrackerManager;

    @Override
    public void onEnable() {
        instance = this;
        // Carrega a configuração padrão se não existir
        saveDefaultConfig();

        // Registramos o Logger do plugin na API para que a Infra possa usá-lo
        // java.util.logging.Logger é uma classe padrão do Java, então a API aceita.
        CoreRegistry.register(java.util.logging.Logger.class, this.getLogger());

        // Inicializa constantes do Bukkit
        BukkitConstants.init(this);

        // Inicializa a Infraestrutura (Banco de Dados)
        if (!setupInfra()) {
            return; // Se falhar, para tudo e evitar erro "zip file closed"
        }

        // Inicializa Managers
        // Inicializa o Cache (Fundação do Gameplay)
        cacheManager = new CacheManager();
        guildManager = new GuildManager();
        economyManager = new EconomyManager(this, cacheManager);
        cooldownManager = new CooldownManager();
        skillManager = new SkillManager();
        damageTrackerManager = new DamageTrackerManager();
        mobManager = new MobManager(this);
        NPCManager npcManager = new NPCManager(this);
        // Carrega os NPCs
        npcManager.loadNPCs();
        // Inicializa ItemGenerator
        ItemGenerator itemGenerator = new ItemGenerator(this);
        lootManager = new LootManager(this, itemGenerator);

        // Registra no Registry (Para comandos e eventos usarem)
        CoreRegistry.register(NPCManager.class, npcManager);
        CoreRegistry.register(CacheManager.class, cacheManager); // Uso interno (ex: CoreMMO-Gameplay)
        CoreRegistry.register(CacheService.class, cacheManager); // Uso externo (ex: CoreMMO-Dungeons)
        CoreRegistry.register(GuildManager.class, guildManager);
        CoreRegistry.register(CooldownManager.class, cooldownManager);
        // Registra o Serviço de Economia
        // Agora, qualquer lugar do código pode chamar:
        // CoreRegistry.get(EconomyService.class).pay(...)
        CoreRegistry.register(EconomyService.class, economyManager);
        // REGISTRA NA API
        CoreRegistry.register(MobService.class, mobManager);
        // Opcional: registrar a classe concreta se precisar de métodos internos
        CoreRegistry.register(MobManager.class, mobManager);

        scoreboardManager = new ScoreboardManager();
        CoreRegistry.register(ScoreboardManager.class, scoreboardManager);

        registerEvents();
        registerCommands();

        initTasks();

        getLogger().info("CoreMMO iniciado com arquitetura Open Core!");
    }

    @Override
    public void onDisable() {
        // Forma elegante e segura de fechar (não gera 'spam' de erro)
        CoreRegistry.getSafe(IDatabase.class).ifPresent(db -> {
            try {
                db.close();
                getLogger().info("Banco de dados desconectado.");
            } catch (Exception e) {
                getLogger().warning("Erro ao fechar conexão: " + e.getMessage());
            }
        });
    }

    private boolean setupInfra() {
        getLogger().info("Conectando ao banco de dados...");

        // Lê as credenciais do config.yml
        String host = getConfig().getString("database.host", "localhost");
        String port = getConfig().getString("database.port", "3306");
        String dbName = getConfig().getString("database.name", "coremmo_db");
        String user = getConfig().getString("database.user", "root");
        String pass = getConfig().getString("database.password", "");

        try {
            // Registra o Banco
            IDatabase databaseProvider = new HikariDatabaseProvider(host, port, dbName, user, pass);
            // Agora qualquer módulo pode usar:
            // CoreRegistry.get(IDatabase.class).getConnection();
            CoreRegistry.register(IDatabase.class, databaseProvider);

            // Registra e Executa a Migration
            ITableManager tableManager = new MariaDBTableManager();
            CoreRegistry.register(ITableManager.class, tableManager);

            // Executa a criação das tabelas
            tableManager.createTables();

            // Registra classes de acesso ao banco
            CoreRegistry.register(UserDAO.class, new MariaDBUserDAO());
            CoreRegistry.register(GuildDAO.class, new MariaDBGuildDAO());

            return true;

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Não foi possível conectar ao banco de dados.", e);
            // Em caso de falha no banco, geralmente desativamos o plugin para evitar danos
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
    }

    private void registerEvents() {
        // Limbo e Carregamento
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this, cacheManager), this);
        // Menu e Skills
        getServer().getPluginManager().registerEvents(new ClassMenuListener(this, cacheManager), this);
        getServer().getPluginManager().registerEvents(new SkillListener(cacheManager, cooldownManager, skillManager), this);
        // Chat e Combate
        getServer().getPluginManager().registerEvents(new ChatListener(cacheManager, guildManager), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this, cacheManager), this);
        // NPCs
        getServer().getPluginManager().registerEvents(new NPCListener(), this);
        // Mobs
        getServer().getPluginManager().registerEvents(new MobListener(mobManager, cacheManager, scoreboardManager, lootManager, damageTrackerManager), this);
        // Regeneração
        getServer().getPluginManager().registerEvents(new RegenListener(), this);
        // Detecção de Inventário
        getServer().getPluginManager().registerEvents(new InventoryListener(this, cacheManager), this);
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("classe")).setExecutor(new ClassCommand());
        Objects.requireNonNull(getCommand("pagar")).setExecutor(new PayCommand(economyManager));
        Objects.requireNonNull(getCommand("saldo")).setExecutor(new BalanceCommand(cacheManager));
        Objects.requireNonNull(getCommand("guilda")).setExecutor(new GuildCommand(this, guildManager, cacheManager));
        Objects.requireNonNull(getCommand("reloadmmo")).setExecutor(new ReloadCommand(this, lootManager));
        Objects.requireNonNull(getCommand("stats")).setExecutor(new StatsCommand(cacheManager));
    }

    private void initTasks() {
        // AutoSave: A cada 5 minutos (6000 ticks)
        // Delay inicial: 5 minutos
        new AutoSaveTask(this, cacheManager).runTaskTimer(this, 6000L, 6000L);
        // Regeneração: A cada 1 segundo (20 ticks)
        // Delay inicial: 1 segundo
        new RegenTask(cacheManager).runTaskTimer(this, 20L, 20L);
    }

    public static CorePlugin getInstance() {
        return instance;
    }
}