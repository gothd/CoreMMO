package br.com.ruasvivas.api.service;

import java.util.UUID;

public interface EconomyService {

    /**
     * Tenta realizar uma transferência segura entre jogadores.
     * @param payerId UUID de quem paga
     * @param receiverId UUID de quem recebe
     * @param amount Quantidade (deve ser positiva)
     * @return true se a transação for bem-sucedida
     */
    boolean pay(UUID payerId, UUID receiverId, long amount);

    // Futuro: Métodos como deposit(), withdraw(), getBalance() entraram aqui
}