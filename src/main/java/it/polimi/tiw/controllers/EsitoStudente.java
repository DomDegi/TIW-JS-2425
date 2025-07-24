package it.polimi.tiw.controllers;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.beans.StudenteAppelloBean;
import it.polimi.tiw.beans.UtenteBean;
import it.polimi.tiw.dao.StudenteDAO;
import it.polimi.tiw.utilities.DBConnection;

@WebServlet("/esito")
public class EsitoStudente extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public void init() throws ServletException {
        this.connection = DBConnection.getConnection(getServletContext());
        ServletContext servletContext = getServletContext();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utente") == null) {
            response.sendRedirect(request.getContextPath() + "/index.html");
            return;
        }
        UtenteBean utente = (UtenteBean) session.getAttribute("utente");
        if (!utente.getRuolo().equals("studente")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Utente non autorizzato\"}");
            return;
        }
        String appelloIdParam = request.getParameter("appelloId");
        int appelloId;
        try {
            appelloId = Integer.parseInt(appelloIdParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "L'id dell'appello non è valido");
            return;
        }
        StudenteDAO studenteDAO = new StudenteDAO(connection, utente.getIDUtente());
        StudenteAppelloBean infoAppello = null;
        try {
            infoAppello = studenteDAO.getInfoAppello(appelloId);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile recuperare l'esito dell'appello");
            return;
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore interno del server");
            return;
        }
        // Converti il bean in una mappa per gestire le date come stringhe
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("matricola", infoAppello.getMatricola());
        result.put("id_studente", infoAppello.getIDStudente());
        result.put("id_appello", infoAppello.getIDAppello());
        result.put("id_corso", infoAppello.getIDCorso());
        result.put("nome", infoAppello.getNome());
        result.put("cognome", infoAppello.getCognome());
        result.put("email", infoAppello.getEmail());
        result.put("corsolaurea", infoAppello.getCorsolaurea());
        result.put("nome_corso", infoAppello.getNomeCorso());
        result.put("voto", infoAppello.getVoto());
        String dataString = null;
        if (infoAppello.getData() != null) {
            try {
                dataString = infoAppello.getData().toString().split(" ")[0];
            } catch (Exception e) {
                dataString = infoAppello.getData().toString();
            }
        }
        result.put("data", dataString);
        result.put("statodivalutazione", infoAppello.getStatoDiValutazione() != null ? infoAppello.getStatoDiValutazione().toString() : null);
        
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(result);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(json);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utente") == null) {
            response.sendRedirect(request.getContextPath() + "/index.html");
            return;
        }
        UtenteBean utente = (UtenteBean) session.getAttribute("utente");
        if (!utente.getRuolo().equals("studente")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Utente non autorizzato\"}");
            return;
        }
        String appelloIdParam = request.getParameter("appelloId");
        int appelloId;
        try {
            appelloId = Integer.parseInt(appelloIdParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "L'id dell'appello non è valido");
            return;
        }
        StudenteDAO studenteDAO = new StudenteDAO(connection, utente.getIDUtente());
        try {
            studenteDAO.setRifiutato(appelloId);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile rifiutare il voto");
            return;
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore interno del server");
            return;
        }
        doGet(request, response);
    }
}
