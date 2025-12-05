package br.com.ruasvivas.coreMMO.economia;

import br.com.ruasvivas.coreMMO.CoreMMO;
import br.com.ruasvivas.coreMMO.model.DadosJogador;

import java.util.UUID;

public class GerenteEconomia {

    private final CoreMMO plugin;

    public GerenteEconomia(CoreMMO plugin) {
        this.plugin = plugin;
    }

    // Método seguro de transferência
    // Retorna TRUE se deu certo, FALSE se falhou (sem saldo/erro)
    public boolean pagar(UUID pagadorID, UUID recebedorID, long valor) {

        // 1. Regra: Valor deve ser positivo
        if (valor <= 0) return false;

        // 2. Regra: Não pode pagar a si mesmo
        if (pagadorID.equals(recebedorID)) return false;

        // Buscamos os dados na RAM (Cache)
        DadosJogador pagador = plugin.getGerenteDados().getDados(pagadorID);
        DadosJogador recebedor = plugin.getGerenteDados().getDados(recebedorID);

        // 3. Regra: Ambos devem estar online (para simplificar este tutorial)
        if (pagador == null || recebedor == null) return false;

        // 4. Regra: Saldo Suficiente
        if (pagador.getMoedas() < valor) return false;

        // --- A TRANSAÇÃO ATÔMICA ---
        // Retira de um, coloca no outro instantaneamente
        pagador.setMoedas(pagador.getMoedas() - valor);
        recebedor.setMoedas(recebedor.getMoedas() + valor);

        return true;
    }
}