package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.beans.AppelloBean;

public class CorsoDAO {
    private Connection con;
    private int id_corso;

    public CorsoDAO(Connection connection, int id_corso) {
        this.con = connection;
        this.id_corso = id_corso;
    }

    public List<AppelloBean> cercaAppelli() throws SQLException {
        List<AppelloBean> appelli = new ArrayList<>();
        String query = "SELECT id_appello, data FROM appello WHERE id_corso = ? ORDER BY data DESC;";
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, this.id_corso);
            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next()) {
                    AppelloBean appello = new AppelloBean();
                    appello.setIDAppello(result.getInt("id_appello"));
                    appello.setIDCorso(this.id_corso);
                    appello.setData(result.getDate("data"));
                    appelli.add(appello);
                }
            }
        }
        return appelli;
    }

    public int cercaIdDocentePerCorso() throws SQLException {
        String query = "SELECT id_docente FROM corso WHERE id_corso = ?;";
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, this.id_corso);
            try (ResultSet result = pstatement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("id_docente");
                } else {
                    throw new SQLException("No docente found for id_corso = " + this.id_corso);
                }
            }
        }
    }
} 