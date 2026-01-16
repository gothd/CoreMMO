package br.com.ruasvivas.gameplay.manager;

import br.com.ruasvivas.api.CoreRegistry;
import br.com.ruasvivas.api.dao.UserDAO;
import br.com.ruasvivas.api.service.EconomyService;
import br.com.ruasvivas.common.model.User;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.logging.Logger;

public class EconomyManager implements EconomyService {

    private final JavaPlugin plugin;
    private final CacheManager cacheManager;

    public EconomyManager(JavaPlugin plugin, CacheManager cacheManager) {
        this.plugin = plugin;
        this.cacheManager = cacheManager;
    }

    @Override
    public boolean pay(UUID payerId, UUID receiverId, long amount) {
        // 1. Validações Básicas
        if (amount <= 0) return false;
        if (payerId.equals(receiverId)) return false;

        // Busca no Cache (Memória)
        // O sistema pressupõe que ambos estão online para a troca
        User payer = cacheManager.getUser(payerId);
        User receiver = cacheManager.getUser(receiverId);

        if (payer == null || receiver == null) {
            return false; // Um dos dois não está carregado/online
        }

        // Verifica Saldo (Regra de Negócio)
        if (payer.getCoins() < amount) {
            return false;
        }

        // Transação Atômica (Em Memória)
        // synchronized garante que threads não dupliquem dinheiro
        synchronized (this) {
            payer.setCoins(payer.getCoins() - amount);
            receiver.setCoins(receiver.getCoins() + amount);
        }

        // Persistência Assíncrona
        // Salva no banco logo em seguida para não perder dados se o servidor cair
        saveAsync(payer);
        saveAsync(receiver);

        // Log para auditoria
        getLogger().info(String.format("[Economia] %s pagou %d para %s",
                payer.getUsername(), amount, receiver.getUsername()));

        return true;
    }

    // Helper para salvar sem travar o servidor
    private void saveAsync(User user) {
        // Cria uma tarefa assíncrona no Bukkit Scheduler
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            // Aqui dentro estamos fora da Main Thread.
            CoreRegistry.getSafe(UserDAO.class).ifPresent(dao -> {
                boolean success = dao.saveUser(user);
                if (!success) {
                    getLogger().warning("Falha ao salvar dados de economia para: " + user.getUsername());
                }
            });

        });
    }

    private Logger getLogger() {
        return CoreRegistry.getSafe(Logger.class).orElse(Bukkit.getLogger());
    }
}