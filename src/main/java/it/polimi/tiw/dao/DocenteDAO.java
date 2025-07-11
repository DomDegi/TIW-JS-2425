package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.beans.CorsoBean;
import it.polimi.tiw.beans.DocenteVerbaleBean;

public class DocenteDAO {
    private Connection con;
    private int idDocente;

    public DocenteDAO(Connection connection, int idDocente) {
        this.con = connection;
        this.idDocente = idDocente;
    }

    // Cerca i corsi di un certo docente
    public List<CorsoBean> cercaCorsi() throws SQLException {
        List<CorsoBean> corsi = new ArrayList<>();
        String query = "SELECT id_corso, nome, cfu FROM corso WHERE id_docente = ? ORDER BY nome DESC;";
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, this.idDocente);
            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    CorsoBean corso = new CorsoBean();
                    corso.setIDCorso(result.getInt("id_corso"));
                    corso.setNome(result.getString("nome"));
                    corso.setCfu(result.getInt("cfu"));
                    corso.setIDDocente(this.idDocente);
                    corsi.add(corso);
                }
            }
        }
        return corsi;
    }

    // Cerca i verbali di un certo docente
    public List<DocenteVerbaleBean> cercaVerbali() throws SQLException {
        List<DocenteVerbaleBean> verbali = new ArrayList<>();
        String query = "SELECT c.nome AS nome_corso, v.data_ora AS dataVerbale, a.data AS dataAppello " +
                "FROM verbale v " +
                "JOIN appello a ON v.id_appello = a.id_appello " +
                "JOIN corso c ON a.id_corso = c.id_corso " +
                "WHERE c.id_docente = ? " +
                "ORDER BY c.nome ASC, a.data ASC;";
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, this.idDocente);
            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    DocenteVerbaleBean verbale = new DocenteVerbaleBean();
                    verbale.setNomeCorso(result.getString("nome_corso"));
                    verbale.setDataVerbale(result.getDate("dataVerbale"));
                    verbale.setDataAppello(result.getDate("dataAppello"));
                    verbali.add(verbale);
                }
            }
        }
        return verbali;
    }
} 