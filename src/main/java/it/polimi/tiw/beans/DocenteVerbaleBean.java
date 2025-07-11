package it.polimi.tiw.beans;

import java.util.Date;

public class DocenteVerbaleBean {
    private String nome_corso;
    private Date dataVerbale;
    private Date dataAppello;

    public DocenteVerbaleBean() {}

    public DocenteVerbaleBean(String nome_corso, Date dataVerbale, Date dataAppello) {
        this.nome_corso = nome_corso;
        this.dataVerbale = dataVerbale;
        this.dataAppello = dataAppello;
    }

    public String getNomeCorso() { return nome_corso; }
    public void setNomeCorso(String nome_corso) { this.nome_corso = nome_corso; }

    public Date getDataVerbale() { return dataVerbale; }
    public void setDataVerbale(Date dataVerbale) { this.dataVerbale = dataVerbale; }

    public Date getDataAppello() { return dataAppello; }
    public void setDataAppello(Date dataAppello) { this.dataAppello = dataAppello; }
}
