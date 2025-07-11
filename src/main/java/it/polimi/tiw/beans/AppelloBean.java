package it.polimi.tiw.beans;

import java.sql.Date;

public class AppelloBean{
	
	public int id_appello;
	private int id_corso;
	public Date data;
	
	public AppelloBean() {};
	
	public AppelloBean(int id_appello, int id_corso, Date data) {
		this.id_appello = id_appello;
		this.id_corso = id_corso;
		this.data = data;
	}
	
	public int getIDAppello() { return id_appello; }
	public void setIDAppello(int id_appello) { this.id_appello = id_appello; }
	
	public int getIDCorso() { return id_corso; }
	public void setIDCorso(int id_corso) { this.id_corso = id_corso; }
	
	public Date getData() {	return data; }
	public void setData(Date data) { this.data = data; }
	
}