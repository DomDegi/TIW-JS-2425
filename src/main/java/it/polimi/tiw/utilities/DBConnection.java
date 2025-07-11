package it.polimi.tiw.utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import jakarta.servlet.ServletContext;
import jakarta.servlet.UnavailableException;

public class DBConnection {
	
	public static Connection getConnection(ServletContext context) throws UnavailableException{
		
		Connection connection = null;
		
		try {
			
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);
			
		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Impossibile caricare i driver del database");
		} catch (SQLException e) {
			throw new UnavailableException("Impossibile ottenere una connessione al database");
		}
		
		return connection;
	}
	
	public static void closeConnection(Connection connection) throws SQLException {
		
		if (connection != null) {
			connection.close();
		}
	}
}