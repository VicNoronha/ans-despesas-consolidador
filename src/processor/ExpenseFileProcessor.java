package processor;

import model.ExpenseRecord;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class ExpenseFileProcessor {

    private final CsvProcessor csvProcessor = new CsvProcessor();
    private final TxtProcessor txtProcessor = new TxtProcessor();
    private final XlsxProcessor xlsxProcessor = new XlsxProcessor();

    public List<ExpenseRecord> processar(File arquivo) {
        String nome = arquivo.getName().toLowerCase();

        try {
            if (nome.endsWith(".csv")) {
                return csvProcessor.processar(arquivo);
            }
            if (nome.endsWith(".txt")) {
                return txtProcessor.processar(arquivo);
            }
            if (nome.endsWith(".xlsx")) {
                return xlsxProcessor.processar(arquivo);
            }
        } catch (Exception e) {
            System.err.println("Falha ao processar arquivo: " + arquivo.getName() + " | " + e.getMessage());
        }

        return Collections.emptyList();
    }
}
