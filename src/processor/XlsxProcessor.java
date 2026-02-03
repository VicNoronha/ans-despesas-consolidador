package processor;

import model.ExpenseRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class XlsxProcessor {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public List<ExpenseRecord> processar(File arquivo) {

        List<ExpenseRecord> registros = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(arquivo);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(0);

            if (header == null) return registros;
            //Descobrir colunas principais
            int idxCnpj = encontrarIndice(header, "CNPJ");
            int idxRazao = encontrarIndice(header, "Razao");
            int idxData = encontrarIndice(header, "Data");
            int idxValor = encontrarIndice(header, "Valor");

            // Se não achar pelo nome exato, tenta outras alternativas
            if (idxRazao == -1) idxRazao = encontrarIndice(header, "RazaoSocial");
            if (idxValor == -1) idxValor = encontrarIndice(header, "Despesa");
            if (idxData == -1) idxData = encontrarIndice(header, "Compet");
            if (idxData == -1) idxData = encontrarIndice(header, "Periodo");

            // Valida se ainda falta algo
            if (idxCnpj == -1 || idxRazao == -1 || idxData == -1 || idxValor == -1) {
                System.err.println("XLSX sem colunas essenciais: " + arquivo.getName());
                return registros;
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                String cnpj = lerComoTexto(row.getCell(idxCnpj));
                String razao = lerComoTexto(row.getCell(idxRazao));
                String data = lerDataComoString(row.getCell(idxData));
                BigDecimal valor = lerComoBigDecimal(row.getCell(idxValor));

                // Se faltar algum campo mínimo, ignora a linha
                if (cnpj.isBlank() || razao.isBlank() || data.isBlank() || valor == null) {
                    continue;
                }

                int ano;
                int trimestre;

                try {
                    ano = extrairAno(data);
                    trimestre = extrairTrimestre(data);
                } catch (Exception e) {
                    // Data inconsistente: deixa para o consolidator contar/ignorar (ou ignora aqui)
                    continue;
                }

                ExpenseRecord registro = new ExpenseRecord(
                        cnpj,
                        razao,
                        ano,
                        trimestre,
                        valor
                );

                registros.add(registro);
            }

        } catch (Exception e) {
            System.err.println("Erro ao processar XLSX: " + arquivo.getName() + " | " + e.getMessage());
            e.printStackTrace();
        }

        return registros;
    }

    private int encontrarIndice(Row header, String nome) {
        for (Cell cell : header) {
            String valor = lerComoTexto(cell);
            if (valor.toUpperCase().contains(nome.toUpperCase())) {
                return cell.getColumnIndex();
            }
        }
        return -1;
    }

    private String lerComoTexto(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                double n = cell.getNumericCellValue();
                long inteiro = (long) n;
                // se for inteiro, retorna como long; senão retorna como String do número
                if (Math.abs(n - inteiro) < 0.0000001) {
                    return String.valueOf(inteiro);
                }
                return String.valueOf(n);

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                // tenta ler resultado da fórmula
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    try {
                        double nv = cell.getNumericCellValue();
                        long inteiro2 = (long) nv;
                        if (Math.abs(nv - inteiro2) < 0.0000001) {
                            return String.valueOf(inteiro2);
                        }
                        return String.valueOf(nv);
                    } catch (Exception ex) {
                        return "";
                    }
                }

            default:
                return "";
        }
    }

    private String lerDataComoString(Cell cell) {
        if (cell == null) return "";

        // Excel pode guardar data como NUMERIC
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return DATE_FORMAT.format(cell.getDateCellValue());
        }

        // Às vezes vem como texto "2023-08-01" ou "202308"
        String texto = lerComoTexto(cell);
        if (texto.isBlank()) return "";

        // Se vier como "YYYYMM" ou "YYYYMMDD", tenta converter para "YYYY-MM-DD"
        if (texto.matches("\\d{6}")) {
            return texto.substring(0, 4) + "-" + texto.substring(4, 6) + "-01";
        }
        if (texto.matches("\\d{8}")) {
            return texto.substring(0, 4) + "-" + texto.substring(4, 6) + "-" + texto.substring(6, 8);
        }

        return texto;
    }

    private BigDecimal lerComoBigDecimal(Cell cell) {
        if (cell == null) return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            }
            if (cell.getCellType() == CellType.STRING) {
                String txt = cell.getStringCellValue().trim();
                if (txt.isEmpty()) return null;
                // trata "1.234,56" ou "1234,56"
                txt = txt.replace(".", "").replace(",", ".");
                return new BigDecimal(txt);
            }
            if (cell.getCellType() == CellType.FORMULA) {
                try {
                    return BigDecimal.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    String txt = cell.getStringCellValue().trim();
                    if (txt.isEmpty()) return null;
                    txt = txt.replace(".", "").replace(",", ".");
                    return new BigDecimal(txt);
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private int extrairAno(String data) {
        // espera YYYY-MM-DD ou YYYY-MM
        return Integer.parseInt(data.substring(0, 4));
    }

    private int extrairTrimestre(String data) {
        // espera YYYY-MM-DD ou YYYY-MM
        int mes = Integer.parseInt(data.substring(5, 7));
        return (mes - 1) / 3 + 1;
    }
}
