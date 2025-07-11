package it.polimi.tiw.beans;

public class DocenteBean extends UtenteBean{
	
	public DocenteBean() {};
	
	public DocenteBean(int id_docente, String email, String nome, String cognome) {
		super(id_docente,email,nome,cognome);
	}
}
