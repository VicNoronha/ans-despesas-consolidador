package util;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {

    public static void extrairZip(File zipFile, File destino) throws IOException {

        byte[] buffer = new byte[8192];

        if (!destino.exists()) {
            destino.mkdirs();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {

            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {

                File novoArquivo = new File(destino, entry.getName());

                if (entry.isDirectory()) {
                    novoArquivo.mkdirs();
                    continue;
                }

                // garante que os diretórios pais existam
                new File(novoArquivo.getParent()).mkdirs();

                try (FileOutputStream fos = new FileOutputStream(novoArquivo)) {

                    int len; // Qts bytes foram lidos no momento
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
        }
    }
}
