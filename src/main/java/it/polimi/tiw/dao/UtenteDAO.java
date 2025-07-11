package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.beans.DocenteBean;
import it.polimi.tiw.beans.StudenteBean;
import it.polimi.tiw.beans.UtenteBean;

public class UtenteDAO {

	private Connection con;

	public UtenteDAO(Connection connection) {
		this.con = connection;
	}

	public UtenteBean checkCredentials(String email, String password) throws SQLException {
		//Query di check credenziali
        String query = "SELECT id_utente, nome, cognome FROM utente WHERE email = ? AND password = ?";     
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, email);
			pstatement.setString(2, password);
			
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.next()) { 
					int id = result.getInt("id_utente");
					String nome = result.getString("nome");
					String cognome = result.getString("cognome");
					
					// Controllo se è uno studente
	                String studenteQuery = "SELECT matricola, corso_laurea FROM studente WHERE id_studente = ?";
	                try (PreparedStatement psStudente = con.prepareStatement(studenteQuery)) {
	                    psStudente.setInt(1, id);
	                    try (ResultSet rsStudente = psStudente.executeQuery()) {
	                        if (rsStudente.next()) {
	                            String matricola = rsStudente.getString("matricola");
	                            String corsoLaurea = rsStudente.getString("corso_laurea");
	                            return new StudenteBean(id, email, nome, cognome, matricola, corsoLaurea);
	                        }
	                    }
	                }
	                
	                //Controllo se è un docente
	                String docenteQuery = "SELECT 1 FROM docente WHERE id_docente = ?";
	                try (PreparedStatement psDocente = con.prepareStatement(docenteQuery)) {
	                    psDocente.setInt(1, id);
	                    try (ResultSet rsDocente = psDocente.executeQuery()) {
	                        if (rsDocente.next()) {
	                            return new DocenteBean(id, email, nome, cognome);
	                        }
	                    }
	                }
	                //Trovato un utente generico nella query (errore)
	                return null;
				}
				else {
					return null; //check delle credenziali fallito
				}
			}
		}
	}
}