import api.AnsClient;
import download.ZipDownloader;
import model.ExpenseRecord;
import processor.CsvExporter;
import processor.ExpenseConsolidator;
import processor.ExpenseFileProcessor;
import processor.FileSelector;
import util.ZipCompressor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        try {
            System.out.println("Iniciando pipeline ANS - Despesas...");

            // 1) Buscar URLs dos 3 ZIPs mais recentes
            AnsClient ansClient = new AnsClient();
            List<String> zips = ansClient.buscarUltimosZips(3);
            System.out.println("ZIPs encontrados: " + zips);

            if (zips.isEmpty()) {
                System.out.println("Nenhum ZIP encontrado. Encerrando.");
                return;
            }

            // 2) Baixar e extrair ZIPs
            ZipDownloader downloader = new ZipDownloader();
            for (String urlZip : zips) {
                System.out.println("Baixando e extraindo: " + urlZip);
                downloader.baixarEExtrair(urlZip);
            }

            // 3) Selecionar arquivos extraídos (somente pasta extracted)
            List<File> arquivos = FileSelector.selecionarArquivosDeDespesa(new File("data/extracted"));
            System.out.println("Arquivos selecionados: " + arquivos.size());

            // 4) Processar arquivos
            ExpenseFileProcessor processor = new ExpenseFileProcessor();
            List<ExpenseRecord> registros = new ArrayList<>();
            for (File arquivo : arquivos) {
                registros.addAll(processor.processar(arquivo));
            }
            System.out.println("Registros lidos (normalizados): " + registros.size());

            // 5) Consolidar + contadores de inconsistências
            ExpenseConsolidator consolidator = new ExpenseConsolidator();
            ExpenseConsolidator.ConsolidationResult resultado = consolidator.consolidar(registros);

            System.out.println("Registros consolidados: " + resultado.consolidados.size());
            System.out.println("Datas inválidas ignoradas: " + resultado.datasInvalidasIgnoradas);
            System.out.println("Valores <= 0 (suspeitos): " + resultado.valoresSuspeitos);
            System.out.println("CNPJ com razão social divergente: " + resultado.razoesDivergentes);

            // 6) Carregar cadastro (REGISTRO_OPERADORA -> CNPJ/Razao)
            Map<String, String[]> mapaOperadoras = carregarMapaOperadoras();
            System.out.println("Operadoras carregadas (map): " + mapaOperadoras.size());

            List<ExpenseRecord> finalList = new ArrayList<>();
            for (ExpenseRecord r : resultado.consolidados) {

                // No CsvProcessor, guardamos REG_ANS/REGISTRO_OPERADORA no campo "cnpj" temporariamente
                String reg = r.getCnpj();
                String[] info = mapaOperadoras.get(reg); // {cnpj, razao}

                if (info != null) {
                    finalList.add(new ExpenseRecord(
                            info[0],
                            info[1],
                            r.getAno(),
                            r.getTrimestre(),
                            r.getValorDespesas()
                    ));
                } else {
                    finalList.add(new ExpenseRecord(
                            reg,
                            "OPERADORA_NAO_ENCONTRADA",
                            r.getAno(),
                            r.getTrimestre(),
                            r.getValorDespesas()
                    ));
                }
            }

            // 7) Exportar CSV final
            String csvFinal = "consolidado_despesas.csv";
            CsvExporter exporter = new CsvExporter();
            exporter.exportar(finalList, csvFinal);
            System.out.println("CSV gerado: " + csvFinal);

            // 8) Compactar CSV em ZIP final
            String zipFinal = "consolidado_despesas.zip";
            ZipCompressor.compactar(csvFinal, zipFinal);
            System.out.println("ZIP gerado: " + zipFinal);

            System.out.println("Processo finalizado com sucesso!");

        } catch (Exception e) {
            System.err.println("Erro no processamento: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Métodos auxiliares


    private static Map<String, String[]> carregarMapaOperadoras() {
        // String[] = {cnpj, razaoSocial}
        Map<String, String[]> mapa = new HashMap<>();

        // Cadastro de operadoras
        String url = "https://dadosabertos.ans.gov.br/FTP/PDA/operadoras_de_plano_de_saude_ativas/Relatorio_cadop.csv";

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {

            String cabecalho = reader.readLine();
            if (cabecalho == null) return mapa;

            String delim = cabecalho.contains(";") ? ";" : ",";
            String[] cols = cabecalho.split(delim);

            // Aceita variações de nomes das colunas
            int idxReg = encontrarIndiceVariacoes(cols, "reg_ans", "registro_operadora", "registrooperadora");
            int idxCnpj = encontrarIndiceVariacoes(cols, "cnpj");
            int idxRazao = encontrarIndiceVariacoes(cols, "razao_social", "razaosocial", "razao");

            if (idxReg == -1 || idxCnpj == -1 || idxRazao == -1) {
                System.out.println("Cadastro operadoras: colunas essenciais não encontradas.");
                System.out.println("Cabecalho: " + cabecalho);
                return mapa;
            }

            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] c = linha.split(delim);
                int max = Math.max(idxReg, Math.max(idxCnpj, idxRazao));
                if (c.length <= max) continue;

                String reg = limpar(c[idxReg]);
                String cnpj = limpar(c[idxCnpj]);
                String razao = limpar(c[idxRazao]);

                if (!reg.isEmpty()) {
                    mapa.put(reg, new String[]{cnpj, razao});
                }
            }

        } catch (Exception e) {
            System.out.println("Falha ao baixar cadastro de operadoras: " + e.getMessage());
        }

        return mapa;
    }

    private static int encontrarIndiceVariacoes(String[] cabecalho, String... possiveisNomes) {
        for (int i = 0; i < cabecalho.length; i++) {
            String col = normalizarNomeColuna(cabecalho[i]);
            for (String alvo : possiveisNomes) {
                if (col.contains(normalizarNomeColuna(alvo))) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static String normalizarNomeColuna(String s) {
        if (s == null) return "";
        return s.replace("\"", "")
                .trim()
                .toLowerCase()
                .replace("_", "")
                .replace(" ", "");
    }

    private static String limpar(String s) {
        return s == null ? "" : s.replace("\"", "").trim();
    }
}
