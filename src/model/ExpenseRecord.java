package model;

import java.math.BigDecimal;

public class ExpenseRecord {

    private String cnpj;
    private String razaoSocial;
    private int ano;
    private int trimestre;
    private BigDecimal valorDespesas;




    public ExpenseRecord(String cnpj,
                         String razaoSocial,
                         int ano,
                         int trimestre,
                         BigDecimal valorDespesas) {

        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.ano = ano;
        this.trimestre = trimestre;
        this.valorDespesas = valorDespesas;

    }


    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public int getTrimestre() {
        return trimestre;
    }

    public BigDecimal getValorDespesas() {
        return valorDespesas;
    }


    public void setTrimestre(int trimestre) {
        this.trimestre = trimestre;
    }


}
