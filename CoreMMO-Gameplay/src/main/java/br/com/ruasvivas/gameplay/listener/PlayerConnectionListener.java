package br.com.ruasvivas.gameplay.listener;

import br.com.ruasvivas.api.CoreRegistry;
import br.com.ruasvivas.api.dao.UserDAO;
import br.com.ruasvivas.common.model.User;
import br.com.ruasvivas.gameplay.manager.CacheManager;
import br.com.ruasvivas.gameplay.ui.ScoreboardManager; // Vamos criar este em breve
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getWorld;

public class PlayerConnectionListener implements Listener {

    private final JavaPlugin plugin;
    private final CacheManager cacheManager;
    private final Logger logger;

    public PlayerConnectionListener(JavaPlugin plugin, CacheManager cacheManager) {
        this.plugin = plugin;
        this.cacheManager = cacheManager;
        this.logger = CoreRegistry.getSafe(Logger.class).orElse(plugin.getLogger());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // ESTADO DE LIMBO (Imediato)
        // Bloqueia o jogador visualmente e logicamente enquanto carregamos os dados
        cacheManager.setLoading(uuid, true);

        // Efeitos infinitos (99999 ticks) e potentes
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 99999, 5));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 99999, 255));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 99999, 255));

        player.sendActionBar(Component.text("Carregando personagem...").color(NamedTextColor.YELLOW));

        // CARREGAMENTO ASSÍNCRONO (Back-end)
        // Sai da thread principal para não travar o servidor
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {

            UserDAO dao = CoreRegistry.get(UserDAO.class);
            User user = null;

            try {
                // Tenta carregar. Se não existir, cria.
                user = dao.loadUser(uuid);
                if (user == null) {
                    dao.createUser(uuid, player.getName());
                    user = dao.loadUser(uuid); // Recarrega o recém-criado
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Erro crítico ao carregar dados de: " + player.getName(), e);
            }

            // RETORNO À MAIN THREAD (Sync)
            // Agora que temos os dados, voltamos para liberar o jogador
            final User dadosFinais = user;

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                // Se o jogador saiu durante o loading, cancela tudo
                if (!player.isOnline()) return;

                if (dadosFinais == null) {
                    player.kick(Component.text("Falha ao carregar seu perfil. Tente novamente."));
                    return;
                }

                // A. Salva no Cache RAM
                cacheManager.cacheUser(dadosFinais);

                // B. Teleporte para última localização
                if (dadosFinais.getWorldName() != null) {
                    World mundo = getWorld(dadosFinais.getWorldName());
                    if (mundo != null) {
                        player.teleport(new Location(mundo, dadosFinais.getX(), dadosFinais.getY(), dadosFinais.getZ(), dadosFinais.getYaw(), dadosFinais.getPitch()));
                    }
                }

                // C. A LIBERAÇÃO (Remove o Limbo)
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.removePotionEffect(PotionEffectType.SLOWNESS);
                player.removePotionEffect(PotionEffectType.RESISTANCE);

                // Destrava proteção de dano e movimento
                cacheManager.setLoading(uuid, false);

                // Atualiza Action Bar (Vida/Mana)
                cacheManager.updateActionBar(player);

                // D. FEEDBACK VISUAL (Boas-vindas)
                enviarFeedbackBoasVindas(player, dadosFinais);

                // E. Inicia Sistemas Auxiliares
                new ScoreboardManager().createScoreboard(player);

                logger.info("Jornada iniciada para: " + player.getName());
            });
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Pega do Cache (RAM)
        User user = cacheManager.getUser(player);
        // Limpa o Cache imediatamente para liberar RAM
        cacheManager.removeUser(uuid);

        if (user != null) {
            // Atualiza localização no objeto antes de salvar
            Location loc = player.getLocation();
            user.setLocation(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

            // Salva no Banco (Assíncrono)
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                CoreRegistry.getSafe(UserDAO.class).ifPresent(dao -> {
                    if (dao.saveUser(user)) {
                        // Log opcional (útil para debug)
                         logger.info("Dados salvos na saída: " + player.getName());
                    } else {
                        logger.warning("Erro ao salvar dados na saída: " + player.getName());
                    }
                });
            });
        }
    }

    // --- PROTEÇÕES DURANTE O LOADING ---

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Se estiver no limbo, é imortal
            if (cacheManager.isLoading(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        // Impede sair do lugar enquanto carrega (evita cair no void se o chunk demorar)
        if (cacheManager.isLoading(event.getPlayer().getUniqueId())) {
            if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) {
                event.setCancelled(true);
            }
        }
    }

    // --- AUXILIARES ---

    private void enviarFeedbackBoasVindas(Player player, User user) {
        player.sendMessage(Component.text("Lenda carregada com sucesso!").color(NamedTextColor.GREEN));

        Component title = Component.text("BEM-VINDO").color(NamedTextColor.GOLD);
        // Usa o nome traduzido da classe (Guerreiro, Mago...)
        Component subtitle = Component.text("Sua jornada começa agora, " + user.getRpgClass().getDisplayName());

        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(1000));

        player.showTitle(Title.title(title, subtitle, times));
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
    }
}