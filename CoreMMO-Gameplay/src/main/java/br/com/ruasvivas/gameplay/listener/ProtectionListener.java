package br.com.ruasvivas.gameplay.listener;

import br.com.ruasvivas.common.model.region.Region;
import br.com.ruasvivas.common.model.region.RegionType;
import br.com.ruasvivas.gameplay.CorePlugin;
import br.com.ruasvivas.gameplay.manager.RegionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Iterator;
import java.util.Optional;

public class ProtectionListener implements Listener {

    private final RegionManager regionManager;

    public ProtectionListener(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    // --- PROTEÇÃO DE BLOCOS (Construir e Quebrar) ---

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!regionManager.canBuild(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Você não pode quebrar blocos nesta região.").color(NamedTextColor.RED));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!regionManager.canBuild(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Você não pode construir nesta região.").color(NamedTextColor.RED));
        }
    }

    // --- PROTEÇÃO DE COMBATE (PvP e Safe Zones) ---
    // TRAVA PACIFISTA (Anti-Cheese de Dano)
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {

        // Regra A: O Atacante está tentando "snipar" de dentro da área segura?
        Player attacker = getAttacker(event.getDamager());
        if (attacker != null) {
            Optional<Region> attackerRegion = regionManager.getRegionAt(attacker.getLocation());
            if (attackerRegion.isPresent() && attackerRegion.get().getType() == RegionType.SAFE_ZONE) {
                event.setCancelled(true);
                attacker.sendMessage(Component.text("Você não pode atacar enquanto estiver em uma Zona Segura!").color(NamedTextColor.RED));
                return;
            }
        }

        // Regra B: A Vítima está protegida?
        if (event.getEntity() instanceof Player victim) {
            Optional<Region> optRegion = regionManager.getRegionAt(victim.getLocation());
            if (optRegion.isPresent()) {
                Region region = optRegion.get();

                // DEFESA CONTRA MOBS
                if (!region.getType().isAllowMobSpawn()) {
                    // Se foi um monstro corpo-a-corpo (Zumbi, Aranha)
                    if (event.getDamager() instanceof Mob mob) {
                        event.setCancelled(true);
                        // Agenda a remoção para o próximo tick
                        Bukkit.getScheduler().runTask(CorePlugin.getInstance(), mob::remove);
                        return;
                    }
                    // Se foi um monstro atirador (Esqueleto atirando flecha)
                    else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Mob shooterMob) {
                        event.setCancelled(true);
                        // Remove a flecha E o atirador no próximo tick
                        Bukkit.getScheduler().runTask(CorePlugin.getInstance(), () -> {
                            proj.remove();
                            shooterMob.remove();
                        });
                        return;
                    }
                }

                // DEFESA CONTRA PVP
                if (!region.getType().isAllowPvP() && attacker != null && attacker != victim) {
                    event.setCancelled(true);
                    attacker.sendMessage(Component.text("PvP está desativado nesta região!").color(NamedTextColor.RED));
                }
            }
        }
    }

    // TRAVA DE INTERESSE (Aggro / Leash)
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMobTarget(EntityTargetLivingEntityEvent event) {
        // Se um monstro tentar focar em um jogador...
        if (event.getTarget() instanceof Player player) {
            Optional<Region> optRegion = regionManager.getRegionAt(player.getLocation());

            if (optRegion.isPresent() && !optRegion.get().getType().isAllowMobSpawn()) {
                event.setCancelled(true); // Impede de adquirir o alvo
            }
        }
    }

    // IMORTALIDADE AMBIENTAL EM SAFE ZONE (Queda, Fogo, Afogamento, etc)
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGeneralDamage(EntityDamageEvent event) {
        // Se o dano veio de uma entidade (Mob/Flecha/Jogador),
        // ignora aqui para que o método 'onEntityDamage' possa processar!
        if (event instanceof EntityDamageByEntityEvent) return;

        if (event.getEntity() instanceof Player player) {
            Optional<Region> optRegion = regionManager.getRegionAt(player.getLocation());

            // Se estiver na cidade, o jogador é literalmente imortal contra TUDO.
            if (optRegion.isPresent() && optRegion.get().getType() == RegionType.SAFE_ZONE) {
                event.setCancelled(true);
            }
        }
    }

    // --- CONTROLE DE SPAWN DE MOBS ---

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent event) {
        // Sempre permite que o próprio plugin force o spawn de algo (ex: Bosses de Quest)
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;

        Optional<Region> optRegion = regionManager.getRegionAt(event.getLocation());
        if (optRegion.isPresent()) {
            Region region = optRegion.get();
            // Se a região (como a Cidade) não permite mobs, cancela o spawn natural
            if (!region.getType().isAllowMobSpawn()) {
                event.setCancelled(true);
            }
        }
    }

    // --- PROTEÇÃO AMBIENTAL (Griefing) ---

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        // Remove da lista de explosão (Creepers/TNT) os blocos que estão protegidos
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Block block = it.next();
            Optional<Region> optRegion = regionManager.getRegionAt(block.getLocation());
            if (optRegion.isPresent()) {
                RegionType type = optRegion.get().getType();
                if (type == RegionType.SAFE_ZONE || type == RegionType.DUNGEON) {
                    it.remove(); // Salva o bloco da destruição
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFireSpread(BlockIgniteEvent event) {
        // Impede que isqueiros ou lava espalhem fogo na cidade
        Optional<Region> optRegion = regionManager.getRegionAt(event.getBlock().getLocation());
        if (optRegion.isPresent() && optRegion.get().getType() == RegionType.SAFE_ZONE) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFluidFlow(BlockFromToEvent event) {
        // Impede que Água ou Lava escorram de fora para dentro de uma Safe Zone ou Dungeon
        Block targetBlock = event.getToBlock();
        Optional<Region> optRegion = regionManager.getRegionAt(targetBlock.getLocation());

        if (optRegion.isPresent()) {
            RegionType type = optRegion.get().getType();
            if (type == RegionType.SAFE_ZONE || type == RegionType.DUNGEON) {
                // Se a fonte for do Wilderness e estiver tentando invadir a região, bloqueia
                Optional<Region> sourceRegion = regionManager.getRegionAt(event.getBlock().getLocation());
                if (sourceRegion.isEmpty() || !sourceRegion.get().getId().equals(optRegion.get().getId())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    // --- UTILITÁRIO ---

    /**
     * Identifica se o dano veio diretamente de um jogador (Espada)
     * ou indiretamente (Flecha, Bola de Fogo, Tridente).
     */
    private Player getAttacker(Entity damager) {
        if (damager instanceof Player p) return p;
        if (damager instanceof Projectile proj) {
            ProjectileSource source = proj.getShooter();
            if (source instanceof Player p) return p;
        }
        return null;
    }
}