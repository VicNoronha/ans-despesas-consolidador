package processor;

import model.ExpenseRecord;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CsvProcessor {

    public List<ExpenseRecord> processar(File arquivo) {

        List<ExpenseRecord> registros = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {

            String cabecalhoLinha = reader.readLine();
            if (cabecalhoLinha == null) return registros;

            String delimitador = cabecalhoLinha.contains(";") ? ";" : ",";
            String[] cabecalho = cabecalhoLinha.split(delimitador);

            int idxData = encontrarIndice(cabecalho, "DATA");
            int idxReg = encontrarIndice(cabecalho, "REG_ANS");
            int idxDesc = encontrarIndice(cabecalho, "DESCRICAO");
            int idxFinal = encontrarIndice(cabecalho, "VL_SALDO_FINAL"); // N depende da ordem fixa das colunas

            if (idxData == -1 || idxReg == -1 || idxDesc == -1 || idxFinal == -1) { // Descobrir a posição da coluna
                System.err.println("CSV ignorado (colunas essenciais não encontradas): " + arquivo.getName());
                System.err.println("Cabecalho: " + cabecalhoLinha);
                return registros;
            }

            String linha;
            while ((linha = reader.readLine()) != null) {

                String[] col = linha.split(delimitador);
                int maxIdx = Math.max(Math.max(idxData, idxReg), Math.max(idxDesc, idxFinal)); // garantir que tenham linhas suficientes
                if (col.length <= maxIdx) continue;

                //Extrai valores da linha
                String data = limpar(col[idxData]);
                String regAns = limpar(col[idxReg]);
                String descricao = limpar(col[idxDesc]).toLowerCase();
                BigDecimal valorFinal = parseBigDecimal(limpar(col[idxFinal]));

                if (regAns.isEmpty() || data.isEmpty() || valorFinal == null) continue;

                // filtro simples: mantém apenas linhas ligadas a eventos/sinistros
                if (!ehDespesaEventosSinistros(descricao)) continue;

                int ano;
                int trimestre;
                try {
                    ano = extrairAno(data);
                    trimestre = extrairTrimestre(data);
                } catch (Exception e) {
                    // data inconsistente -> ignora a linha
                    continue;
                }

                // Guarda REG_ANS no campo "cnpj" temporariamente.
                registros.add(new ExpenseRecord(regAns, "PENDENTE", ano, trimestre, valorFinal));
            }

        } catch (Exception e) {
            System.err.println("Erro ao ler CSV: " + arquivo.getName() + " | " + e.getMessage());
        }

        return registros;
    }

    private boolean ehDespesaEventosSinistros(String descricao) {
        return descricao.contains("evento") || descricao.contains("sinistro");
    }

    private int encontrarIndice(String[] cabecalho, String nome) {
        for (int i = 0; i < cabecalho.length; i++) {
            String col = limpar(cabecalho[i]).toUpperCase();
            if (col.contains(nome.toUpperCase())) return i;
        }
        return -1;
    }

    private String limpar(String s) {
        return s == null ? "" : s.replace("\"", "").trim();
    }

    private BigDecimal parseBigDecimal(String txt) {
        if (txt == null || txt.isEmpty()) return null;

        // trata "1.234,56" e "1234,56"
        txt = txt.replace(".", "").replace(",", ".");
        try {
            return new BigDecimal(txt);
        } catch (Exception e) {
            return null;
        }
    }

    private int extrairAno(String data) {
        // "2025-03-31" -> 2025
        return Integer.parseInt(data.substring(0, 4));
    }

    private int extrairTrimestre(String data) {
        // "2025-03-31" -> mes=03 -> trimestre 1
        int mes = Integer.parseInt(data.substring(5, 7));
        return (mes - 1) / 3 + 1;
    }
}

