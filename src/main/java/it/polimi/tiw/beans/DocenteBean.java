package it.polimi.tiw.beans;

public final class DocenteBean extends UtenteBean{
	
	public DocenteBean() {};
	
	public DocenteBean(int id_docente, String email, String nome, String cognome) {
		super(id_docente,email,nome,cognome);
	}
	
	@Override
	public String getRuolo() { return "docente"; }
}
