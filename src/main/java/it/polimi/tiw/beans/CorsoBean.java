package it.polimi.tiw.beans;

public class CorsoBean{
	
	public int id_corso;
	
	public String nome;
	
	public int cfu;
	
	public int id_docente;
	
	public int getIDCorso() { return id_corso; }
	public void setIDCorso(int id_corso) { this.id_corso = id_corso; }
	
	public String getNome() { return nome; }
	public void setNome(String nome) { this.nome = nome; }
	
	public int getCfu() { return cfu; }
	public void setCfu(int cfu) { this.cfu = cfu; }
	
	public int getIDDocente() { return id_docente; }
	public void setIDDocente(int id_docente) { this.id_docente = id_docente; }
}