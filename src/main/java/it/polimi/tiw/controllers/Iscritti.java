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
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.beans.IscrittiBean;
import it.polimi.tiw.beans.DocenteBean;
import it.polimi.tiw.beans.UtenteBean;
import it.polimi.tiw.dao.AppelloDAO;
import it.polimi.tiw.utilities.DBConnection;

@WebServlet("/iscritti-appello")
public class Iscritti extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public void init() throws ServletException {
        this.connection = DBConnection.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utente") == null) {
            response.sendRedirect(request.getContextPath() + "/index.html");
            return;
        }
        UtenteBean utente = (UtenteBean) session.getAttribute("utente");
        if (!utente.getRuolo().equals("docente")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Utente non autorizzato\"}");
            return;
        }
        DocenteBean docente = (DocenteBean) utente;

        String idAppelloParam = request.getParameter("id_appello");
        int id_appello;
        try {
            id_appello = Integer.parseInt(idAppelloParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Il parametro id_appello deve essere un intero valido");
            return;
        }

        String orderBy = request.getParameter("orderBy");
        String orderDirection = request.getParameter("orderDirection");
        if (orderDirection != null && orderDirection.equalsIgnoreCase("ASC")) {
            orderDirection = "ASC";
        } else {
            orderDirection = "DESC";
        }

        AppelloDAO appelloDAO = new AppelloDAO(connection, id_appello);

        try {
            // Verifica che l'appello appartenga al docente
            int docenteCorretto = appelloDAO.cercaIdDocentePerAppello();
            if (docenteCorretto != docente.getIDUtente()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "L'appello a cui vuoi accedere non è tuo");
                return;
            }

            List<IscrittiBean> iscritti = appelloDAO.cercaIscritti(orderBy, orderDirection);
            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(iscritti);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);

        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Impossibile recuperare gli iscritti a questo appello");
            return;
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore interno del server");
            return;
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utente") == null) {
            response.sendRedirect(request.getContextPath() + "/index.html");
            return;
        }
        UtenteBean utente = (UtenteBean) session.getAttribute("utente");
        if (!utente.getRuolo().equals("docente")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Utente non autorizzato\"}");
            return;
        }
        DocenteBean docente = (DocenteBean) utente;

        String idAppelloParam = request.getParameter("id_appello");
        int id_appello;
        try {
            id_appello = Integer.parseInt(idAppelloParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Il parametro id_appello deve essere un intero valido");
            return;
        }

        AppelloDAO appelloDAO = new AppelloDAO(connection, id_appello);

        try {
            int docenteCorretto = appelloDAO.cercaIdDocentePerAppello();
            if (docenteCorretto != docente.getIDUtente()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "L'appello a cui vuoi accedere non è tuo");
                return;
            }

            appelloDAO.aggiornaPubblicati();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\": true}");

        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile pubblicare i voti");
            return;
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore interno del server");
            return;
        }
    }
} 