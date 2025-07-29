package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import it.polimi.tiw.beans.IscrittiBean;
import it.polimi.tiw.beans.StatoDiValutazione;

public class AppelloDAO {
    private Connection con;
    private int id_appello;

    public AppelloDAO(Connection connection, int id_appello) {
        this.con = connection;
        this.id_appello = id_appello;
    }

    public List<IscrittiBean> cercaIscritti(String orderBy, String orderDirection) throws SQLException {
        List<IscrittiBean> iscrittiList = new ArrayList<>();
        Set<String> allowedOrderBy = Set.of("matricola", "nome", "cognome", "email", "corso_laurea", "voto", "stato_valutazione");
        Set<String> allowedDirections = Set.of("ASC", "DESC");
        if (orderBy == null || !allowedOrderBy.contains(orderBy)) {
            orderBy = "cognome"; // default column
        }
        if (orderDirection == null || !allowedDirections.contains(orderDirection.toUpperCase())) {
            orderDirection = "ASC"; // default direction
        }

        String query = "SELECT s.id_studente, s.matricola, u.cognome, u.nome, u.email, s.corso_laurea, v.voto, v.stato_valutazione " +
                "FROM appello a " +
                "JOIN valutazione v ON a.id_appello = v.id_appello " +
                "JOIN studente s ON v.id_studente = s.id_studente " +
                "JOIN utente u ON s.id_studente = u.id_utente " +
                "WHERE a.id_appello = ? " +
                "ORDER BY " + orderBy + " " + orderDirection;
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, this.id_appello);
            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    IscrittiBean iscritti = new IscrittiBean();
                    iscritti.setIdStudente(result.getInt("id_studente"));
                    iscritti.setMatricola(result.getString("matricola"));
                    iscritti.setCognome(result.getString("cognome"));
                    iscritti.setNome(result.getString("nome"));
                    iscritti.setEmail(result.getString("email"));
                    iscritti.setCorsoLaurea(result.getString("corso_laurea"));
                    iscritti.setVoto(result.getString("voto"));
                    iscritti.setIDAppello(this.id_appello);
                    iscritti.setStatoDiValutazione(StatoDiValutazione.valueOf(result.getString("stato_valutazione")));
                    iscrittiList.add(iscritti);
                }
            }
        }
        return iscrittiList;
    }

    
    
    public void aggiornaPubblicati() throws SQLException {
        String query = "UPDATE valutazione SET stato_valutazione = 'PUBBLICATO' WHERE id_appello = ? AND stato_valutazione = 'INSERITO';";
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, this.id_appello);
            pstatement.executeUpdate();
        }
    }

    public int cercaIdDocentePerAppello() throws SQLException {
        String query = "SELECT c.id_docente FROM appello a JOIN corso c ON a.id_corso = c.id_corso WHERE a.id_appello = ?;";
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, this.id_appello);
            try (ResultSet result = pstatement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("id_docente");
                } else {
                    throw new SQLException("No docente found for id_appello = " + this.id_appello);
                }
            }
        }
    }
} 