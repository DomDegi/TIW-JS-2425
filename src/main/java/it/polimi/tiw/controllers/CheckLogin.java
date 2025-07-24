package it.polimi.tiw.controllers;

import java.io.IOException; 
import java.sql.Connection;
import java.sql.SQLException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import it.polimi.tiw.beans.UtenteBean;
import it.polimi.tiw.beans.DocenteBean;
import it.polimi.tiw.beans.StudenteBean;
import it.polimi.tiw.dao.UtenteDAO;
import it.polimi.tiw.utilities.DBConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class CheckLogin extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public void init() throws ServletException {
        this.connection = DBConnection.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().println("{\"error\": \"Email e password obbligatorie\"}");
            return;
        }

        UtenteDAO utenteDAO = new UtenteDAO(connection);
        UtenteBean utente = null;
        try {
            utente = utenteDAO.checkCredentials(email, password);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().println("{\"error\": \"Errore interno, riprova più tardi\"}");
            return;
        }

        if (utente == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().println("{\"error\": \"Email o password errati\"}");
        } else {
            request.getSession().setAttribute("utente", utente);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            Gson gson = new Gson();
            JsonObject json = gson.toJsonTree(utente).getAsJsonObject();
            switch (utente.getRuolo()) {
            	case "studente" -> json.addProperty("role", "studente");
            	case "docente" -> json.addProperty("role", "docente");
                default -> throw new IllegalStateException("Ruolo sconosciuto: " + utente.getRuolo());
            }
            response.getWriter().println(json.toString());
        }
    }

    public void destroy() {
        try {
            DBConnection.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
