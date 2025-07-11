package it.polimi.tiw.beans;

import java.sql.Date;


public class StudenteAppelloBean {
    private String matricola;
    private int id_studente;
    private int id_appello;
    private int id_corso;
    private String nome;
    private String cognome;
    private String email;
    private String corsolaurea;
    private String nome_corso;
    private String voto;
    private Date data;
    private StatoDiValutazione statoDiValutazione;

    public StudenteAppelloBean() {}

    public String getMatricola() {
        return matricola;
    }

    public void setMatricola(String matricola) {
        this.matricola = matricola;
    }
    public int getIDStudente() {
        return id_studente;
    }

    public void setIDStudente(int id_studente) {
        this.id_studente = id_studente;
    }

    public int getIDAppello() {
        return id_appello;
    }

    public void setIDAppello(int id_appello) {
        this.id_appello = id_appello;
    }

    public int getIDCorso() {
        return id_corso;
    }

    public void setIDCorso(int id_corso) {
        this.id_corso = id_corso;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCorsolaurea() {
        return corsolaurea;
    }

    public void setCorsolaurea(String corsolaurea) {
        this.corsolaurea = corsolaurea;
    }

    public String getNomeCorso() {
        return nome_corso;
    }

    public void setNomeCorso(String nome_corso) {
        this.nome_corso = nome_corso;
    }

    public String getVoto() {
        return voto;
    }

    public void setVoto(String voto) {
        this.voto = voto;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public StatoDiValutazione getStatoDiValutazione() {
        return statoDiValutazione;
    }

    public void setStatoDiValutazione(StatoDiValutazione statodivalutazione) {
        this.statoDiValutazione = statodivalutazione;
    }
}
