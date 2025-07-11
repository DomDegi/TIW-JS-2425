package it.polimi.tiw.beans;

public abstract class UtenteBean {
	
	private int id_utente;
	private String email;
	private String nome;
	private String cognome;
	
	public UtenteBean() {};
	
	public UtenteBean(int id_utente, String email, String nome, String cognome) {
		this.id_utente = id_utente;
		this.email = email;
		this.nome = nome;
		this.cognome = cognome;
	}
	
	public int getIDUtente() { return id_utente; }
	public void setIDUtente(int id_utente) { this.id_utente = id_utente; }
	
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
		
	public String getNome() { return nome; }
	public void setNome(String nome) { this.nome = nome; }
	
	public String getCognome() { return cognome; }
	public void setCognome(String cognome) { this.cognome = cognome;}
	
	
}