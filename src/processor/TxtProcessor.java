package processor;

import model.ExpenseRecord;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TxtProcessor {

    public List<ExpenseRecord> processar(File arquivo) {

        List<ExpenseRecord> registros = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {

            String linha = reader.readLine(); // cabeçalho
            if (linha == null) return registros;

            String delimitador = linha.contains(";") ? ";" : "\\|";
            String[] cabecalho = linha.split(delimitador);

            int idxCnpj = encontrarIndice(cabecalho, "CNPJ");
            int idxRazao = encontrarIndice(cabecalho, "Razao");
            int idxData = encontrarIndice(cabecalho, "Data");
            int idxValor = encontrarIndice(cabecalho, "Valor");

            while ((linha = reader.readLine()) != null) {

                String[] colunas = linha.split(delimitador); // Separa as colunas e valida tamanho

                if (colunas.length <= idxValor) continue;

                int ano = extrairAno(colunas[idxData]);
                int trimestre = extrairTrimestre(colunas[idxData]);

                BigDecimal valor = new BigDecimal(colunas[idxValor]);

                ExpenseRecord registro = new ExpenseRecord(
                        colunas[idxCnpj],
                        colunas[idxRazao],
                        ano,
                        trimestre,
                        valor
                );

                registros.add(registro);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return registros;
    }

    private int encontrarIndice(String[] cabecalho, String nome) {
        for (int i = 0; i < cabecalho.length; i++) {
            if (cabecalho[i].toUpperCase().contains(nome.toUpperCase())) {
                return i;
            }
        }
        return -1;
    }

    private int extrairAno(String data) {
        return Integer.parseInt(data.substring(0, 4));
    }

    private int extrairTrimestre(String data) {
        int mes = Integer.parseInt(data.substring(5, 7));
        return (mes - 1) / 3 + 1;
    }
}
