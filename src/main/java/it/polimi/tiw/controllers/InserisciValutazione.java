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
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;

import it.polimi.tiw.beans.StudenteAppelloBean;
import it.polimi.tiw.beans.DocenteBean;
import it.polimi.tiw.beans.UtenteBean;
import it.polimi.tiw.dao.AppelloDAO;
import it.polimi.tiw.dao.StudenteDAO;
import it.polimi.tiw.utilities.DBConnection;

@WebServlet("/inserisci-valutazione")
public class InserisciValutazione extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public void init() throws ServletException {
        this.connection = DBConnection.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utente") == null) {
            response.sendRedirect("index.html");
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

        String id_studente_param = request.getParameter("id_studente");
        String id_appello_param = request.getParameter("id_appello");
        int id_studente, id_appello;
        try {
            id_studente = Integer.parseInt(id_studente_param);
            id_appello = Integer.parseInt(id_appello_param);
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "I parametri devono essere interi validi");
            return;
        }

        AppelloDAO appelloDAO = new AppelloDAO(connection, id_appello);
        try {
            int docenteCorretto = appelloDAO.cercaIdDocentePerAppello();
            if (docenteCorretto != docente.getIDUtente()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "L'appello a cui vuoi accedere non è tuo");
                return;
            }
            StudenteDAO studenteDAO = new StudenteDAO(connection, id_studente);
            List<Integer> studenti = studenteDAO.getStudentiIscrittiAppello(id_appello);
            if (!studenti.contains(id_studente)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Lo studente a cui vuoi accedere non ha dato questo esame");
                return;
            }
            StudenteAppelloBean infostud = studenteDAO.getInfoAppello(id_appello);
            Gson gson = new Gson();
            String json = gson.toJson(infostud);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Impossibile recuperare i dati di questo studente per questo appello");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utente") == null) {
            response.sendRedirect("index.html");
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

        String id_studente_param = request.getParameter("id_studente");
        String id_appello_param = request.getParameter("id_appello");
        String voto = request.getParameter("voto");
        int id_studente, id_appello;
        try {
            id_studente = Integer.parseInt(id_studente_param);
            id_appello = Integer.parseInt(id_appello_param);
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "I parametri devono essere interi validi");
            return;
        }
        if (voto == null || voto.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Il voto non può essere vuoto");
            return;
        }
        Set<String> votiValidi = Set.of("18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
                "30", "30L", "assente", "riprovato");
        if (!votiValidi.contains(voto)) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\": \"Voto non accettato, riprova\"}");
            return;
        }
        AppelloDAO appelloDAO = new AppelloDAO(connection, id_appello);
        try {
            int docenteCorretto = appelloDAO.cercaIdDocentePerAppello();
            if (docenteCorretto != docente.getIDUtente()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "L'appello a cui vuoi accedere non è tuo");
                return;
            }
            StudenteDAO studenteDAO = new StudenteDAO(connection, id_studente);
            List<Integer> studenti = studenteDAO.getStudentiIscrittiAppello(id_appello);
            if (!studenti.contains(id_studente)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Lo studente a cui vuoi accedere non ha dato questo esame");
                return;
            }
            studenteDAO.setVotoEStato(id_appello, voto);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\": true}");
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile modificare il voto");
        }
    }
} 