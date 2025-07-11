package it.polimi.tiw.controllers;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.beans.IscrittiBean;
import it.polimi.tiw.beans.DocenteBean;
import it.polimi.tiw.dao.AppelloDAO;
import it.polimi.tiw.dao.ValutazioneDAO;
import it.polimi.tiw.utilities.DBConnection;

@WebServlet("/iscritti-appello")
public class Iscritti extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;

    public void init() throws ServletException {
        this.connection = DBConnection.getConnection(getServletContext());
        ServletContext servletContext = getServletContext();
        JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(servletContext);
        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(webApplication);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        DocenteBean docente = (DocenteBean) request.getSession().getAttribute("utente");
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
        IWebExchange webExchange = application.buildExchange(request, response);
        WebContext ctx = new WebContext(webExchange, request.getLocale());
        try {
            String idAppelloParam = request.getParameter("id_appello");
            int id_appello = Integer.parseInt(idAppelloParam);

            String orderBy = request.getParameter("orderBy");
            String orderDirection = request.getParameter("orderDirection");
            if (orderDirection != null && orderDirection.equalsIgnoreCase("ASC")) {
                orderDirection = "ASC";
            } else {
                orderDirection = "DESC";
            }

            AppelloDAO appelloDAO = new AppelloDAO(connection, id_appello);
            int docenteCorretto = appelloDAO.cercaIdDocentePerAppello();
            if (docenteCorretto != docente.getIDUtente()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "L'appello a cui vuoi accedere non è tuo");
                return;
            }

            List<IscrittiBean> iscritti = appelloDAO.cercaIscritti(orderBy, orderDirection);
            ctx.setVariable("iscritti", iscritti);
            ctx.setVariable("orderBy", orderBy);
            ctx.setVariable("orderDirection", orderDirection);

            // Controlla se ci sono studenti da verbalizzare (PUBBLICATO o RIFIUTATO)
            ValutazioneDAO valutazioneDAO = new ValutazioneDAO(connection, id_appello);
            List<Integer> studentiDaVerbalizzare = valutazioneDAO.getIDStudentiPubbORif();
            boolean ciSonoStudentiDaVerbalizzare = !studentiDaVerbalizzare.isEmpty();
            ctx.setVariable("ciSonoStudentiDaVerbalizzare", ciSonoStudentiDaVerbalizzare);

        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Impossibile recuperare gli iscritti a questo appello");
            return;
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Il parametro id_appello deve essere un intero valido");
            return;
        }
        templateEngine.process("/WEB-INF/iscritti-appello.html", ctx, response.getWriter());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String idAppelloParam = request.getParameter("id_appello");
            int id_appello = Integer.parseInt(idAppelloParam);
            AppelloDAO appelloDAO = new AppelloDAO(connection, id_appello);
            appelloDAO.aggiornaPubblicati();
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile pubblicare i voti");
            return;
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Il parametro id_appello deve essere un intero valido");
            return;
        }
        doGet(request, response);
    }
} 