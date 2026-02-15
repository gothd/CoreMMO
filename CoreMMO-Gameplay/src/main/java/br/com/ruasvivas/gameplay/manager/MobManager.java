package br.com.ruasvivas.gameplay.manager;

import br.com.ruasvivas.api.service.MobService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import java.util.Random;

public class MobManager implements MobService {

    private final JavaPlugin plugin;
    private final Random random = new Random();
    // Chaves públicas para acesso externo se necessário
    public final NamespacedKey LEVEL_KEY;
    public final NamespacedKey BASE_HEALTH_KEY;
    public final NamespacedKey BASE_DAMAGE_KEY;

    public MobManager(JavaPlugin plugin) {
        this.plugin = plugin;
        // Cria uma chave única: "coremmo:level"
        this.LEVEL_KEY = new NamespacedKey(plugin, "level");
        this.BASE_HEALTH_KEY = new NamespacedKey(plugin, "base_health");
        this.BASE_DAMAGE_KEY = new NamespacedKey(plugin, "base_damage");
    }

    @Override
    public int getMobLevel(LivingEntity entity) {
        if (entity == null) return 1;
        Integer level = entity.getPersistentDataContainer().get(LEVEL_KEY, PersistentDataType.INTEGER);
        // Retorna o valor ou 1 se for nulo (Safe Unboxing)
        return level != null ? level : 1;
    }

    public void setupMob(LivingEntity entity) {
        if (!(entity instanceof Monster)) return; // Só afeta monstros hostis

        PersistentDataContainer pdc = entity.getPersistentDataContainer();

        // Determina o Nível (Se já tiver nível, mantém para não resetar mob existente)
        int level;
        if (pdc.has(LEVEL_KEY, PersistentDataType.INTEGER)) {
            Integer storedLevel = pdc.get(LEVEL_KEY, PersistentDataType.INTEGER);
            level = storedLevel != null ? storedLevel : 1;
        } else {
            // TODO: V1.1 -> Implementar lógica de distância do spawn aqui
            level = random.nextInt(10) + 1;
            pdc.set(LEVEL_KEY, PersistentDataType.INTEGER, level);
        }

        // Aplica Escalonamento de Atributos
        applyScaling(entity, level);

        // Atualiza Visual
        updateName(entity, level);
    }

    private void applyScaling(LivingEntity entity, int level) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();

        // --- VIDA (HEALTH) ---
        AttributeInstance healthAttr = entity.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttr != null) {
            double baseHealth;

            // Se ainda não temos o valor base salvo no NBT, salvamos agora (Snapshot do Vanilla)
            if (!pdc.has(BASE_HEALTH_KEY, PersistentDataType.DOUBLE)) {
                baseHealth = healthAttr.getBaseValue();
                pdc.set(BASE_HEALTH_KEY, PersistentDataType.DOUBLE, baseHealth);
            } else {
                // Se já temos, lemos do NBT para evitar multiplicar o valor já modificado
                Double storedBase = pdc.get(BASE_HEALTH_KEY, PersistentDataType.DOUBLE);
                baseHealth = storedBase != null ? storedBase : healthAttr.getBaseValue();
            }

            // FÓRMULA DE ESCALONAMENTO (Quadrática)
            // Vida: Base * (1 + 0.3*Lvl + 0.05*Lvl²) -> Lvl 10 = 9x Vida | Lvl 20 = 27x Vida
            double multiplier = 1.0 + (level * 0.3) + (Math.pow(level, 2) * 0.05);

            double newMaxHealth = baseHealth * multiplier;
            healthAttr.setBaseValue(newMaxHealth);

            // Cura o mob para a nova vida máxima (senão ele fica com a vida antiga)
            entity.setHealth(newMaxHealth);
        }

        // --- DANO (DAMAGE) ---
        AttributeInstance damageAttr = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        if (damageAttr != null) {
            double baseDamage;

            if (!pdc.has(BASE_DAMAGE_KEY, PersistentDataType.DOUBLE)) {
                baseDamage = damageAttr.getBaseValue();
                pdc.set(BASE_DAMAGE_KEY, PersistentDataType.DOUBLE, baseDamage);
            } else {
                Double storedBase = pdc.get(BASE_DAMAGE_KEY, PersistentDataType.DOUBLE);
                baseDamage = storedBase != null ? storedBase : damageAttr.getBaseValue();
            }

            // Dano escala mais devagar que a vida para não dar One-Hit kill em jogadores
            // Dano: Base * (1 + 0.15*Lvl) -> Linear mas forte. Lvl 10 = 2.5x Dano
            double damageMultiplier = 1.0 + (level * 0.15);

            damageAttr.setBaseValue(baseDamage * damageMultiplier);
        }
    }

    public void updateName(LivingEntity entity) {
        // Recalcula o nível se não passar como argumento
        int level = getMobLevel(entity);
        updateName(entity, level);
    }

    private void updateName(LivingEntity entity, int level) {
        // Cor do Nível (Identificação de Perigo)
        NamedTextColor levelColor;
        boolean isBoss = false;

        if (level >= 20) {
            levelColor = NamedTextColor.DARK_PURPLE;
            isBoss = true;
        } else if (level >= 10) {
            levelColor = NamedTextColor.RED;
        } else if (level >= 5) {
            levelColor = NamedTextColor.GOLD;
        } else {
            levelColor = NamedTextColor.YELLOW;
        }

        // Cálculo da Porcentagem de Vida
        double currentHp = entity.getHealth();
        AttributeInstance maxHpAttr = entity.getAttribute(Attribute.MAX_HEALTH);
        NamedTextColor heartColor = getHeartColor(maxHpAttr, currentHp);

        // 4. Montagem do Nome
        Component name = Component.text("[Lv." + level + "] ", levelColor)
                .append(Component.text(entity.getType().name()).color(NamedTextColor.WHITE));

        if (isBoss) {
            // Boss: Mostra números exatos para estratégia (Arredondada para cima para não mostrar 0 se tiver 0.5)
            int hpInt = (int) Math.ceil(currentHp);
            name = name.append(Component.text(" " + hpInt + " ", NamedTextColor.GRAY))
                    .append(Component.text("❤", heartColor));
        } else {
            // Mob Comum: Apenas o coração indicativo
            name = name.append(Component.text(" ❤", heartColor));
        }

        entity.customName(name);
        entity.setCustomNameVisible(true);
    }

    private static @NonNull NamedTextColor getHeartColor(AttributeInstance maxHpAttr, double currentHp) {
        double maxHp = (maxHpAttr != null) ? maxHpAttr.getValue() : currentHp;

        double percent = currentHp / maxHp;

        // Cor do Coração (Imersão)
        NamedTextColor heartColor;
        if (percent > 0.66) {
            heartColor = NamedTextColor.GREEN;       // Saudável (66%+)
        } else if (percent > 0.33) {
            heartColor = NamedTextColor.YELLOW;      // Machucado (33% - 66%)
        } else {
            heartColor = NamedTextColor.RED;         // Crítico (< 33%)
        }
        return heartColor;
    }
}