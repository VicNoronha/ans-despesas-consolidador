package download;

import util.ZipUtils;

import java.io.*;
import java.net.URL;

public class ZipDownloader {

    private static final String BASE_DIR = "data";
    private static final String ZIP_DIR = BASE_DIR + "/zips";
    private static final String EXTRACT_DIR = BASE_DIR + "/extracted";

    // Recebe a URL completa do ZIP e baixa/extrai
    public void baixarEExtrair(String urlZip) throws IOException {

        criarDiretorios();

        String nomeArquivo = extrairNomeArquivo(urlZip);
        File destinoZip = new File(ZIP_DIR + "/" + nomeArquivo);

        baixarArquivo(urlZip, destinoZip);

        String pastaSaida = EXTRACT_DIR + "/" + nomeArquivo.replace(".zip", "");
        ZipUtils.extrairZip(destinoZip, new File(pastaSaida));
    }

    private void criarDiretorios() {
        new File(ZIP_DIR).mkdirs();
        new File(EXTRACT_DIR).mkdirs(); //cria a pasta e tb pastas pai, caso elas não existam
    }

    private String extrairNomeArquivo(String urlZip) {
        return urlZip.substring(urlZip.lastIndexOf("/") + 1);
    }

    private void baixarArquivo(String urlZip, File destino) throws IOException {
        try (InputStream in = new URL(urlZip).openStream(); //usado baixar os xip em partes e evitar vazamentos de recursos
             FileOutputStream out = new FileOutputStream(destino)) {

            byte[] buffer = new byte[8192];
            int bytesLidos;

            while ((bytesLidos = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesLidos);
            }
        }
    }
}



