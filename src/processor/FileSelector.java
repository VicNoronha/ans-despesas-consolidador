package processor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSelector {
    public static List<File> selecionarArquivosDeDespesa(File diretorioBase) {

        List<File> arquivosSelecionados = new ArrayList<>();

        if (!diretorioBase.exists() || !diretorioBase.isDirectory()) {
            return arquivosSelecionados;
        }

        percorrerDiretorio(diretorioBase, arquivosSelecionados); //encontra arquivos, subpastas e add na lista
        return arquivosSelecionados;
    }

    private static void percorrerDiretorio(File diretorio, List<File> resultado) {
        File[] arquivos = diretorio.listFiles();

        if (arquivos == null) {
            return;
        }

        for (File arquivo : arquivos) {
            if (arquivo.isDirectory()) {
                percorrerDiretorio(arquivo, resultado); //recursão
            } else if (ehArquivoDeDespesa(arquivo)) {
                resultado.add(arquivo);
            }
        }
    }

    private static boolean ehArquivoDeDespesa(File arquivo) {
        String nome = arquivo.getName().toLowerCase();

        return nome.endsWith(".csv") ||
                nome.endsWith(".txt") ||
                nome.endsWith(".xlsx");
    }


}
