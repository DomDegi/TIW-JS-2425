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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.beans.DocenteVerbaleBean;
import it.polimi.tiw.beans.DocenteBean;
import it.polimi.tiw.beans.UtenteBean;
import it.polimi.tiw.dao.DocenteDAO;
import it.polimi.tiw.utilities.DBConnection;

@WebServlet("/elenco-verbali")
public class ElencoVerbali extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public ElencoVerbali() {
        super();
    }

    public void init() throws ServletException {
        this.connection = DBConnection.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
        DocenteDAO docenteDAO = new DocenteDAO(connection, docente.getIDUtente());
        try {
            List<DocenteVerbaleBean> infoVerbale = docenteDAO.cercaVerbali();
            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(infoVerbale);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile recuperare i verbali per questo docente");
            return;
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore interno del server");
            return;
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
} 