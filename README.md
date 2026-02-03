### ANS Consolidador de Despesas
O objetivo é automatizar o download das demonstrações contábeis da ANS e extrair os arquivos dos últimos trimestres, identificar inconsistências e por fim, consolidar os dados e gerar um csv final compactado em zip.



###  Sobre o projeto

1. Acessa o diretório público da ANS
2. Identifica automaticamente os 3 ZIPs mais recentes de demonstrações contábeis
3. Baixa e extrai os arquivos
4. Localiza arquivos CSV/TXT/XLSX
5. Processa os dados de despesas (utilizando VL_SALDO_FINAL)
6. Normaliza por Ano e Trimestre
7. Consolida os registros
8. Faz join com cadastro de operadoras para obter CNPJ e Razão Social
9. Gera o arquivo: consolidado_despesas.csv

###  Principais decisões técnicas

Processamento incremental para evitar alto consumo de memória;

Identificação dinâmica das colunas (não depende de ordem fixa);

 Join em memória usando Map;
 
 Utilização do campo VL_SALDO_FINAL como valor de despesa;
 
Valores <= 0 são contabilizados como suspeitos;

 Datas inválidas são ignoradas.

