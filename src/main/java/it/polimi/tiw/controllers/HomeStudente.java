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

import it.polimi.tiw.beans.AppelloBean;
import it.polimi.tiw.beans.CorsoBean;
import it.polimi.tiw.beans.UtenteBean;
import it.polimi.tiw.dao.CorsoDAO;
import it.polimi.tiw.dao.StudenteDAO;
import it.polimi.tiw.utilities.DBConnection;

@WebServlet("/home-studente")
public class HomeStudente extends HttpServlet {
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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Not authenticated\"}");
            return;
        }

        if (!(utente instanceof it.polimi.tiw.beans.StudenteBean)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Not authenticated\"}");
            return;
        }

        String idCorsoParam = request.getParameter("id_corso");
        Gson gson = new GsonBuilder().create();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        try {
            if (idCorsoParam != null) {
                int id_corso = Integer.parseInt(idCorsoParam);
                CorsoDAO corsoDAO = new CorsoDAO(connection, id_corso);
                List<AppelloBean> appelli = corsoDAO.cercaAppelli();
                String json = gson.toJson(appelli);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(json);
                return;
            } else {
                StudenteDAO studenteDAO = new StudenteDAO(connection, utente.getIDUtente());
                List<CorsoBean> corsi = studenteDAO.cercaCorsi();
                String json = gson.toJson(corsi);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(json);
                return;
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid id_corso");
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}

