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
        UtenteBean utente = (session != null) ? (UtenteBean) session.getAttribute("utente") : null;
        if (session == null || utente == null) {
            response.sendRedirect("../login.html");
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
        }
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(infoAppello);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(json);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        UtenteBean utente = (session != null) ? (UtenteBean) session.getAttribute("utente") : null;
        if (session == null || utente == null) {
            response.sendRedirect("../login.html");
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
        }
        doGet(request, response);
    }
}
