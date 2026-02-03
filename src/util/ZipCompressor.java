package util;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipCompressor {
    public static void compactar(String arquivoCsv, String zipFinal) throws IOException {

        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFinal));
        FileInputStream fis = new FileInputStream(arquivoCsv);

        zos.putNextEntry(new ZipEntry(new File(arquivoCsv).getName()));
        // Responsável por copiar os arquivos em partes
        byte[] buffer = new byte[1024];
        int bytes;

        while ((bytes = fis.read(buffer)) > 0) {
            zos.write(buffer, 0, bytes);
        }

        zos.closeEntry();
        zos.close();
        fis.close();
    }
}
