package it.polimi.tiw.beans;

import java.util.Date;
import java.time.LocalTime;

public class VerbaleBean {
    private int id_verbale;
    private String codice;
    private int id_appello;
    private Date dataAppello;
    private Date dataVerbale;
    private LocalTime ora;

    public VerbaleBean() {}

    public VerbaleBean(int id_verbale, String codice, int id_appello, Date dataAppello, Date dataVerbale) {
        this.id_verbale = id_verbale;
        this.codice = codice;
        this.id_appello = id_appello;
        this.dataAppello = dataAppello;
        this.dataVerbale = dataVerbale;
    }

    public int getIDVverbale() { return id_verbale; }
    public void setIDVerbale(int id_verbale) { this.id_verbale = id_verbale; }

    public String getCodice() { return codice; }
    public void setCodice(String codice) { this.codice = codice; }

    public int getIDAppello() { return id_appello; }
    public void setIDAppello(int id_appello) { this.id_appello = id_appello; }

    public Date getDataAppello() { return dataAppello; }
    public void setDataAppello(Date dataAppello) { this.dataAppello = dataAppello; }

    public Date getDataVerbale() { return dataVerbale; }
    public void setDataVerbale(Date dataVerbale) { this.dataVerbale = dataVerbale; }

    public LocalTime getOra() { return ora; }
    public void setOra(LocalTime ora) { this.ora = ora; }
}
