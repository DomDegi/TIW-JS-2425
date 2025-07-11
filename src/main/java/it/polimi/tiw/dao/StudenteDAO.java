package it.polimi.tiw.dao;

import it.polimi.tiw.beans.CorsoBean;
import it.polimi.tiw.beans.AppelloBean;
import it.polimi.tiw.beans.StudenteAppelloBean;
import it.polimi.tiw.beans.StatoDiValutazione;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;



public class StudenteDAO {
	private Connection connection = null;
	private int id_studente;
	
	public StudenteDAO(Connection connection, int id_studente) {
		this.connection = connection;
		this.id_studente = id_studente;
	}

	public List<CorsoBean> cercaCorsi() throws SQLException {
		List<CorsoBean> corsi = new ArrayList<>();
		String query = "SELECT c.id_corso, c.nome, c.cfu "
		             + "FROM iscrizione_corso i "
				     + "JOIN corso c ON i.id_corso = c.id_corso "
                     + "WHERE i.id_studente = ? "
				     + "ORDER BY c.nome;";
		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, id_studente);
			try (ResultSet result = pstatement.executeQuery()) {
				while (result.next()) {
					CorsoBean corso = new CorsoBean();
					corso.setIDCorso(result.getInt("id_corso"));
					corso.setNome(result.getString("nome"));
					corso.setCfu(result.getInt("cfu"));
					corsi.add(corso);
				}
			}
		}
		return corsi;
	}

	// Cerca gli appelli di un determinato corso a cui lo studente è iscritto (cioè per cui esiste una valutazione)
	public List<AppelloBean> cercaAppelliStudente(int id_corso) throws SQLException {
		List<AppelloBean> appelli = new ArrayList<>();
		String query = "SELECT a.id_appello, a.data " +
                       "FROM appello a " +
                       "JOIN valutazione v ON a.id_appello = v.id_appello " +
                       "WHERE a.id_corso = ? AND v.id_studente = ? " +
                       "ORDER BY a.data;";
		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, id_corso);
			pstatement.setInt(2, id_studente);
			try (ResultSet result = pstatement.executeQuery()) {
				while (result.next()) {
					AppelloBean appello = new AppelloBean();
					appello.setIDAppello(result.getInt("id_appello"));
					appello.setIDCorso(id_corso);
					appello.setData(result.getDate("data"));
					appelli.add(appello);
				}
			}
		}
		return appelli;
	}
	
	// Restituisce la lista degli studenti iscritti al corso identificato da id_corso
	public List<Integer> getStudentiIscrittiCorso(int id_corso) throws SQLException {
		List<Integer> studenti = new ArrayList<>();
		String query = "SELECT id_studente "
					 + "FROM iscrizione_corso "
				     + "WHERE id_corso = ?;";
		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, id_corso);
			try (ResultSet result = pstatement.executeQuery()) {
				while (result.next()) {
					studenti.add(result.getInt("id_studente"));
				}
			}
		}
		return studenti;
	}

	// Restituisce la lista degli studenti iscritti all'appello identificato da id_appello
	public List<Integer> getStudentiIscrittiAppello(int id_appello) throws SQLException {
		List<Integer> studenti = new ArrayList<>();
		String query = "SELECT v.id_studente "
					 + "FROM valutazione v "
				     + "WHERE v.id_appello = ?;";
		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, id_appello);
			try (ResultSet result = pstatement.executeQuery()) {
				while (result.next()) {
					studenti.add(result.getInt("id_studente"));
				}
			}
		}
		return studenti;
	}
	
	// Restituisce un oggetto che contiene tutte le informazioni da mostrare nella pagina esito riguardo l'appello dello studente
	public StudenteAppelloBean getInfoAppello(int id_appello) throws SQLException {
		StudenteAppelloBean infoAppello = new StudenteAppelloBean();
		String query = "SELECT " +
				"s.matricola, " +
				"v.id_studente, " +
				"v.id_appello, " +
				"a.id_corso, " +
				"u.nome, " +
				"u.cognome, " +
				"u.email, " +
				"s.corso_laurea, " +
				"c.nome AS nome_corso, " +
				"v.voto, " +
				"a.data, " +
				"v.stato_valutazione " +
			"FROM valutazione v " +
			"JOIN studente s ON v.id_studente = s.id_studente " +
			"JOIN utente u ON s.id_studente = u.id_utente " +
			"JOIN appello a ON v.id_appello = a.id_appello " +
			"JOIN corso c ON a.id_corso = c.id_corso " +
			"WHERE v.id_appello = ? AND v.id_studente = ?;";
		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, id_appello);
			pstatement.setInt(2, this.id_studente); // assuming this.id_utente is the current student
			try (ResultSet result = pstatement.executeQuery()) {
				if (result.next()) {
					infoAppello.setMatricola(result.getString("matricola"));
					infoAppello.setIDStudente(result.getInt("id_studente"));
					infoAppello.setIDAppello(result.getInt("id_appello"));
					infoAppello.setIDCorso(result.getInt("id_corso"));
					infoAppello.setNome(result.getString("nome"));
					infoAppello.setCognome(result.getString("cognome"));
					infoAppello.setEmail(result.getString("email"));
					infoAppello.setCorsolaurea(result.getString("corso_laurea"));
					infoAppello.setNomeCorso(result.getString("nome_corso"));
					infoAppello.setVoto(result.getString("voto"));
					infoAppello.setData(result.getDate("data"));
					infoAppello.setStatoDiValutazione(StatoDiValutazione.valueOf(result.getString("stato_valutazione").toUpperCase()));
				}
			}
		}
		return infoAppello;
	}

	// Imposta lo stato_valutazione a 'rifiutato' per uno studente e un appello
	public void setRifiutato(int id_appello) throws SQLException {
		String query = "UPDATE valutazione SET stato_valutazione = ? "
					 + "WHERE id_studente = ? AND id_appello = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setString(1, "rifiutato");
			pstatement.setInt(2, this.id_studente);
			pstatement.setInt(3, id_appello);
			pstatement.executeUpdate();
		}
	}

	// Aggiorna sia il voto che lo stato_valutazione per uno studente e un appello
	public void setVotoEStato(int id_appello, String voto) throws SQLException {
		String query = "UPDATE valutazione SET voto = ?, stato_valutazione = ? "
					 + "WHERE id_studente = ? AND id_appello = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setString(1, voto);
			pstatement.setString(2, StatoDiValutazione.INSERITO.name());
			pstatement.setInt(3, this.id_studente);
			pstatement.setInt(4, id_appello);
			pstatement.executeUpdate();
		}
	}
	
}
