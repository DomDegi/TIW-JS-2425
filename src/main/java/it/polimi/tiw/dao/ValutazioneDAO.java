package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import it.polimi.tiw.beans.IscrittiBean;
import it.polimi.tiw.beans.StatoDiValutazione;
import it.polimi.tiw.beans.VerbaleBean;

public class ValutazioneDAO {
    private Connection con;
    private int id_appello;

    public ValutazioneDAO(Connection connection, int idAppello) {
        this.con = connection;
        this.id_appello = idAppello;
    }

    public List<Integer> getIDStudentiPubbORif() throws SQLException {
        List<Integer> id_studenti = new ArrayList<>();
        String query = "SELECT v.id_studente " +
                "FROM valutazione v " +
                "WHERE (v.stato_valutazione = 'PUBBLICATO' OR v.stato_valutazione = 'RIFIUTATO') " +
                "AND v.id_appello = ?;";
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, this.id_appello);
            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    id_studenti.add(result.getInt("id_studente"));
                }
            }
        }
        return id_studenti;
    }

    public List<IscrittiBean> getInfoStudentiAggiornati(int appId, List<Integer> studentIds) throws SQLException {
        if (studentIds == null || studentIds.isEmpty()) return new ArrayList<>();
        String inSql = studentIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String query = "SELECT v.id_studente, v.voto, v.stato_valutazione, u.nome, u.cognome, s.matricola, s.corso_laurea, u.email " +
                "FROM valutazione v " +
                "JOIN studente s ON v.id_studente = s.id_studente " +
                "JOIN utente u ON s.id_studente = u.id_utente " +
                "WHERE v.id_appello = ? AND v.id_studente IN (" + inSql + ")";
        try (PreparedStatement pstm = con.prepareStatement(query)) {
            pstm.setInt(1, id_appello);
            for (int i = 0; i < studentIds.size(); i++) {
                pstm.setInt(i + 2, studentIds.get(i));
            }
            try (ResultSet rs = pstm.executeQuery()) {
                List<IscrittiBean> results = new ArrayList<>();
                while (rs.next()) {
                    IscrittiBean info = new IscrittiBean();
                    info.setIdStudente(rs.getInt("id_studente"));
                    info.setMatricola(rs.getString("matricola"));
                    info.setVoto(rs.getString("voto"));
                    info.setStatoDiValutazione(StatoDiValutazione.valueOf(rs.getString("stato_valutazione")));
                    info.setNome(rs.getString("nome"));
                    info.setCognome(rs.getString("cognome"));
                    info.setCorsoLaurea(rs.getString("corso_laurea"));
                    info.setEmail(rs.getString("email"));
                    info.setIDAppello(id_appello);
                    results.add(info);
                }
                return results;
            }
        }
    }

    public void aggiornaVerbalizzato() throws SQLException {
        String query = "UPDATE valutazione " +
                "SET " +
                "voto = CASE WHEN stato_valutazione = 'RIFIUTATO' THEN 'rimandato' ELSE voto END, " +
                "stato_valutazione = CASE WHEN stato_valutazione IN ('PUBBLICATO', 'RIFIUTATO') THEN 'VERBALIZZATO' ELSE stato_valutazione END " +
                "WHERE id_appello = ? AND stato_valutazione IN ('PUBBLICATO', 'RIFIUTATO')";
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, this.id_appello);
            pstatement.executeUpdate();
        }
    }

    public void creaVerbale() throws SQLException {
        String query = "INSERT INTO verbale (data_ora, id_appello, codice_verbale) VALUES (NOW(), ?, ?)";
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, this.id_appello);
            int codiceVerbale = (int) (System.currentTimeMillis() % 1000000);
            pstatement.setInt(2, codiceVerbale);
            pstatement.executeUpdate();
        }
    }   

    public VerbaleBean getUltimoVerbale() throws SQLException {
        VerbaleBean verbale = new VerbaleBean();
        String query = "SELECT v.id_verbale, v.codice_verbale, v.id_appello, a.data AS data_appello, v.data_ora, TIME(v.data_ora) as ora " +
                "FROM verbale v " +
                "JOIN appello a ON v.id_appello = a.id_appello " +
                "WHERE v.id_appello = ? ORDER BY v.id_verbale DESC LIMIT 1";
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, this.id_appello);
            try (ResultSet result = pstatement.executeQuery()) {
                if (result.next()) {
                    verbale.setIDVerbale(result.getInt("id_verbale"));
                    verbale.setCodice(result.getString("codice_verbale"));
                    verbale.setIDAppello(result.getInt("id_appello"));
                    verbale.setDataAppello(result.getDate("data_appello"));
                    verbale.setDataVerbale(result.getTimestamp("data_ora"));
                    verbale.setOra(result.getTime("ora").toLocalTime());
                }
            }
        }
        return verbale;
    }

} 