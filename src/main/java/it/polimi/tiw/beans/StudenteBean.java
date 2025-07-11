package it.polimi.tiw.beans;

public class StudenteBean extends UtenteBean {
	
	private String matricola;
	private String corso_laurea;
	
	public StudenteBean() {};
	
	public StudenteBean(int id_studente, String email, String nome, String cognome, String matricola, String corso_laurea) {
		super(id_studente,email,nome,cognome);
		this.matricola = matricola;
		this.corso_laurea = corso_laurea;
	}
	
	public String getMatricola() { return matricola; }
	public void setMatricola(String matricola) { this.matricola = matricola; }
	
	public String getCorsoLaurea() { return corso_laurea; }
	public void setCorsoLaurea(String corso_laurea) { this.corso_laurea = corso_laurea; }
}

