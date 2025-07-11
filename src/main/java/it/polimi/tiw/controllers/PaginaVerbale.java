package it.polimi.tiw.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.beans.IscrittiBean;
import it.polimi.tiw.beans.VerbaleBean;
import it.polimi.tiw.beans.DocenteBean;
import it.polimi.tiw.dao.AppelloDAO;
import it.polimi.tiw.dao.ValutazioneDAO;
import it.polimi.tiw.utilities.DBConnection;

@WebServlet("/pagina-verbale")
public class PaginaVerbale extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public void init() throws ServletException {
        this.connection = DBConnection.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Metodo non consentito\"}");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utente") == null || !(session.getAttribute("utente") instanceof DocenteBean)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Utente non autorizzato\"}");
            return;
        }
        DocenteBean docente = (DocenteBean) session.getAttribute("utente");

        String id_appello_param = request.getParameter("id_appello");
        int id_appello;
        try {
            id_appello = Integer.parseInt(id_appello_param);
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Il parametro id_appello deve essere un intero valido");
            return;
        }

        AppelloDAO appelloDAO = new AppelloDAO(connection, id_appello);
        try {
            int docenteCorretto = appelloDAO.cercaIdDocentePerAppello();
            if (docenteCorretto != docente.getIDUtente()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Il verbale a cui vuoi accedere non è tuo");
                return;
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel controllo docente");
            return;
        }

        ValutazioneDAO valutazioneDAO = new ValutazioneDAO(connection, id_appello);
        List<Integer> studentiDaAggiornare;
        Map<String, Object> result = new HashMap<>();
        try {
            studentiDaAggiornare = valutazioneDAO.getIDStudentiPubbORif();
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Errore nel cercare gli ID degli studenti da aggiornare.");
            return;
        }
        try {
            valutazioneDAO.aggiornaVerbalizzato();
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Errore durante l'aggiornamento dei dati come verbalizzati.");
            return;
        }
        List<IscrittiBean> studentiAggiornati = new ArrayList<>();
        try {
            studentiAggiornati = valutazioneDAO.getInfoStudentiAggiornati(id_appello, studentiDaAggiornare);
            result.put("infoverbalizzati", studentiAggiornati);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Errore nel recuperare le informazioni degli studenti aggiornati.");
            return;
        }
        try {
            valutazioneDAO.creaVerbale();
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nella creazione del verbale.");
            return;
        }
        VerbaleBean verbale = new VerbaleBean();
        try {
            verbale = valutazioneDAO.getUltimoVerbale();
            result.put("verbale", verbale);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Errore nel recuperare il verbale creato.");
            return;
        }
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(result);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }
}