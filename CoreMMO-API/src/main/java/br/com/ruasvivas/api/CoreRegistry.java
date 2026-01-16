package br.com.ruasvivas.api;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Permite que implementações sejam trocadas em tempo de execução.
 */
public class CoreRegistry {

    private static final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    public static <T> void register(Class<T> interfaceClass, T implementation) {
        services.put(interfaceClass, implementation);
    }

    // Método "Hard": Use quando o serviço É OBRIGATÓRIO para o funcionamento
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> interfaceClass) {
        Object service = services.get(interfaceClass);
        if (service == null) {
            throw new IllegalStateException("Serviço obrigatório não encontrado: " + interfaceClass.getName());
        }
        return (T) service;
    }

    // Método "Soft": Use em onDisable ou para serviços opcionais
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getSafe(Class<T> interfaceClass) {
        return Optional.ofNullable((T) services.get(interfaceClass));
    }
}