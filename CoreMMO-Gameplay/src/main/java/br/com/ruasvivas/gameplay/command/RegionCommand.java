package br.com.ruasvivas.gameplay.command;

import br.com.ruasvivas.api.CoreRegistry;
import br.com.ruasvivas.api.dao.RegionDAO;
import br.com.ruasvivas.common.model.region.Region;
import br.com.ruasvivas.common.model.region.RegionType;
import br.com.ruasvivas.gameplay.manager.RegionManager;
import br.com.ruasvivas.gameplay.manager.SelectionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RegionCommand implements CommandExecutor {

    private final SelectionManager selectionManager;

    public RegionCommand(SelectionManager selectionManager) {
        this.selectionManager = selectionManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!player.hasPermission("coremmo.admin")) {
            player.sendMessage(Component.text("Sem permissão.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("Uso: /region <create|expandvert>").color(NamedTextColor.RED));
            return true;
        }

        // --- EXPAND VERT ---
        if (args[0].equalsIgnoreCase("expandvert")) {
            if (selectionManager.expandVert(player)) {
                player.sendMessage(Component.text("↕ Seleção expandida do fundo do mundo até o céu!").color(NamedTextColor.AQUA));
            } else {
                player.sendMessage(Component.text("Você precisa selecionar os 2 pontos primeiro!").color(NamedTextColor.RED));
            }
            return true;
        }

        // --- COMANDO CREATE ---
        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 3) {
                player.sendMessage(Component.text("Uso: /region create <id> <tipo> [prioridade]").color(NamedTextColor.RED));
                player.sendMessage(Component.text("Tipos: SAFE_ZONE, PVP_ARENA, DUNGEON, CONTESTED_DUNGEON, WILDERNESS").color(NamedTextColor.GRAY));
                return true;
            }

            if (!selectionManager.hasFullSelection(player.getUniqueId())) {
                player.sendMessage(Component.text("Você precisa selecionar 2 pontos com o Machado de Ouro!").color(NamedTextColor.RED));
                return true;
            }

            String id = args[1].toLowerCase();
            RegionType type;

            try {
                type = RegionType.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage(Component.text("Tipo inválido. Use: SAFE_ZONE, DUNGEON...").color(NamedTextColor.RED));
                return true;
            }

            int priority = args.length > 3 ? Integer.parseInt(args[3]) : 0;

            Location loc1 = selectionManager.getPos1(player.getUniqueId());
            Location loc2 = selectionManager.getPos2(player.getUniqueId());

            Region region = new Region(
                    id, type, loc1.getWorld().getName(),
                    loc1.getX(), loc1.getY(), loc1.getZ(),
                    loc2.getX(), loc2.getY(), loc2.getZ(),
                    priority
            );

            CoreRegistry.get(RegionManager.class).registerRegion(region);
            // Salva no Banco de Dados MariaDB (Persistência)
            CoreRegistry.get(RegionDAO.class).saveRegion(region);
            // Limpa a seleção e para os timers
            selectionManager.clearSelection(player.getUniqueId());

            player.sendMessage(Component.text("✅ Região '")
                    .append(Component.text(id).color(NamedTextColor.GOLD))
                    .append(Component.text("' criada e salva no banco de dados! [Prioridade: " + priority + "]")));

            return true;
        }

        return true;
    }
}