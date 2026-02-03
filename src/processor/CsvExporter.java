package processor;
import model.ExpenseRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvExporter {
    public void exportar(List<ExpenseRecord> registros, String caminho) throws IOException {

        FileWriter writer = new FileWriter(caminho);

        writer.write("CNPJ,RazaoSocial,Trimestre,Ano,ValorDespesas\n");

        for (ExpenseRecord r : registros) {
            writer.write(
                    r.getCnpj() + "," +
                            r.getRazaoSocial() + "," +
                            r.getTrimestre() + "," +
                            r.getAno() + "," +
                            r.getValorDespesas() + "\n"
            );
        }

        writer.close();
    }
}
