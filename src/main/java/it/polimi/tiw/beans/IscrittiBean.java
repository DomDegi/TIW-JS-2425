package it.polimi.tiw.beans;

import it.polimi.tiw.beans.StatoDiValutazione;

public class IscrittiBean {
    private int id_studente;
    private String nome;
    private String cognome;
    private String email;
    private String matricola;
    private String corso_laurea;
    private StatoDiValutazione statoDiValutazione;
    private String voto;
    private int id_appello;

    public IscrittiBean() {}

    public IscrittiBean(int id_studente, String nome, String cognome, String email, String matricola, String corso_laurea, StatoDiValutazione statoDiValutazione, String voto, int id_appello) {
        this.id_studente = id_studente;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.matricola = matricola;
        this.corso_laurea = corso_laurea;
        this.statoDiValutazione = statoDiValutazione;
        this.voto = voto;
        this.id_appello = id_appello;
    }

    public int getIdStudente() { return id_studente; }
    public void setIdStudente(int id_studente) { this.id_studente = id_studente; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMatricola() { return matricola; }
    public void setMatricola(String matricola) { this.matricola = matricola; }

    public int getIDAppello() { return id_appello; }
    public void setIDAppello(int id_appello) { this.id_appello = id_appello; }

    public String getCorsoLaurea() { return corso_laurea; }
    public void setCorsoLaurea(String corso_laurea) { this.corso_laurea = corso_laurea; }

    public StatoDiValutazione getStatoDiValutazione() { return statoDiValutazione; }
    public void setStatoDiValutazione(StatoDiValutazione statoDiValutazione) { this.statoDiValutazione = statoDiValutazione; }

    public String getVoto() { return voto; }
    public void setVoto(String voto) { this.voto = voto; }
} 