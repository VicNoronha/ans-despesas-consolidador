package api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnsClient {

    private static final String BASE_URL =
            "https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/";

    // Retorna URLs completas dos ZIPs mais recentes (ex.: .../2025/3T2025.zip)
    public List<String> buscarUltimosZips(int qtd) throws IOException {

        String htmlBase = lerConteudo(BASE_URL);

        // encontrando os anos primeiro: href="2025/"
        Set<String> anosSet = new HashSet<>();
        Matcher mAno = Pattern.compile("href=\"(\\d{4})/\"").matcher(htmlBase); // garante anos únicos
        while (mAno.find()) {
            anosSet.add(mAno.group(1));
        }

        List<String> anos = new ArrayList<>(anosSet); // Com o set não conseguimos ordenar
        anos.sort(Comparator.reverseOrder());

        // procura o zip dentro do ano: arquivos tipo 1T2025.zip, 2T2025.zip...
        List<String> zipsEncontrados = new ArrayList<>();
        Pattern pZip = Pattern.compile("href=\"(([1-4]T)\\d{4}\\.zip)\"");

        for (String ano : anos) {
            String htmlAno = lerConteudo(BASE_URL + ano + "/");

            Matcher mZip = pZip.matcher(htmlAno);
            while (mZip.find()) {
                String nomeZip = mZip.group(1); // ex: 3T2025.zip
                zipsEncontrados.add(BASE_URL + ano + "/" + nomeZip);
            }
        }

        // Ordena do mais recente para o mais antigo (ano desc, trimestre desc)
        zipsEncontrados.sort((a, b) -> compararZipPorData(b, a));

        return zipsEncontrados.size() > qtd ? zipsEncontrados.subList(0, qtd) : zipsEncontrados; // retorna somente a qtd desejada
    }

    // Compara URLs com base no nome do zip (ex.: 3T2025.zip)
    private int compararZipPorData(String urlA, String urlB) {
        String a = urlA.substring(urlA.lastIndexOf("/") + 1); // 3T2025.zip
        String b = urlB.substring(urlB.lastIndexOf("/") + 1);

        int triA = Integer.parseInt(a.substring(0, 1));
        int anoA = Integer.parseInt(a.substring(2, 6));

        int triB = Integer.parseInt(b.substring(0, 1));
        int anoB = Integer.parseInt(b.substring(2, 6));

        if (anoA != anoB) return Integer.compare(anoA, anoB);
        return Integer.compare(triA, triB);
    }

    private String lerConteudo(String url) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
        StringBuilder sb = new StringBuilder();
        String linha;
        while ((linha = reader.readLine()) != null) sb.append(linha);
        reader.close();
        return sb.toString();
    }
}



