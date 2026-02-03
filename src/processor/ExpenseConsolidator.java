package processor;

import model.ExpenseRecord;

import java.math.BigDecimal;
import java.util.*;

public class ExpenseConsolidator {

    public static class ConsolidationResult {
        public final List<ExpenseRecord> consolidados;
        public final int datasInvalidasIgnoradas;
        public final int valoresSuspeitos;
        public final int razoesDivergentes;

        public ConsolidationResult(List<ExpenseRecord> consolidados,
                                   int datasInvalidasIgnoradas,
                                   int valoresSuspeitos,
                                   int razoesDivergentes) {
            this.consolidados = consolidados;
            this.datasInvalidasIgnoradas = datasInvalidasIgnoradas;
            this.valoresSuspeitos = valoresSuspeitos;
            this.razoesDivergentes = razoesDivergentes;
        }
    }

    public ConsolidationResult consolidar(List<ExpenseRecord> registros) {

        Map<String, ExpenseRecord> mapa = new HashMap<>();
        Map<String, String> cnpjParaRazao = new HashMap<>();

        int datasInvalidasIgnoradas = 0;
        int valoresSuspeitos = 0;
        int razoesDivergentes = 0;

        for (ExpenseRecord r : registros) {

            // 1) Data inconsistente -> IGNORA
            if (!dataEhValida(r)) {
                datasInvalidasIgnoradas++;
                continue;
            }

            // 2) CNPJ duplicado com razão social diferente -> MARCA como divergente
            String cnpj = r.getCnpj();
            String razao = r.getRazaoSocial();
            if (!cnpjParaRazao.containsKey(cnpj)) {
                cnpjParaRazao.put(cnpj, razao);
            } else if (!cnpjParaRazao.get(cnpj).equalsIgnoreCase(razao)) {
                razoesDivergentes++;

            }

            // 3) Valor <= 0 -> SUSPEITO (não ignora; só conta)
            if (r.getValorDespesas().compareTo(BigDecimal.ZERO) <= 0) {
                valoresSuspeitos++;
            }

            // Consolidação por chave (cnpj+ano+trimestre)
            String chave = gerarChave(r);

            if (!mapa.containsKey(chave)) {
                mapa.put(chave, r);
            } else {
                ExpenseRecord existente = mapa.get(chave);
                BigDecimal soma = existente.getValorDespesas().add(r.getValorDespesas());

                ExpenseRecord atualizado = new ExpenseRecord(
                        existente.getCnpj(),
                        existente.getRazaoSocial(),
                        existente.getAno(),
                        existente.getTrimestre(),
                        soma
                );
                mapa.put(chave, atualizado);
            }
        }

        return new ConsolidationResult(
                new ArrayList<>(mapa.values()),
                datasInvalidasIgnoradas,
                valoresSuspeitos,
                razoesDivergentes
        );
    }

    private boolean dataEhValida(ExpenseRecord r) {
        return r.getAno() > 2000 && r.getTrimestre() >= 1 && r.getTrimestre() <= 4;
    }

    private String gerarChave(ExpenseRecord r) {
        return r.getCnpj() + "-" + r.getAno() + "-" + r.getTrimestre();
    }
}
