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
import java.util.ArrayList;
import java.util.List;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.beans.UtenteBean;
import it.polimi.tiw.beans.VerbaleBean;
import it.polimi.tiw.beans.IscrittiBean;
import it.polimi.tiw.dao.AppelloDAO;
import it.polimi.tiw.dao.ValutazioneDAO;
import it.polimi.tiw.utilities.DBConnection;

@WebServlet("/pagina-verbale")
public class PaginaVerbale extends HttpServlet {
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
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UtenteBean utente = (UtenteBean) request.getSession().getAttribute("utente");
        ValutazioneDAO valutazioneDAO;
        List<Integer> studentiDaAggiornare;
        int appid;
        VerbaleBean verbale = new VerbaleBean();
        List<IscrittiBean> studentiAggiornati = new ArrayList<>();
        try {
            String appelloIdParam = request.getParameter("id_appello");
            appid = Integer.parseInt(appelloIdParam);

            AppelloDAO appelloDAO = new AppelloDAO(connection, appid);
            int docenteCorretto = appelloDAO.cercaIdDocentePerAppello();
            if (docenteCorretto != utente.getIDUtente()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Il verbale a cui vuoi accedere non è tuo");
                return;
            }

            valutazioneDAO = new ValutazioneDAO(connection, appid);

            // lista formata dagli id degli studenti che hanno lo stato di valutazione a
            // pubblicato o rifiutato
            studentiDaAggiornare = valutazioneDAO.getIDStudentiPubbORif();

            // aggiorno nel database lo stato degli studenti con stato di valutazione
            // mettendolo a verbalizzato
            // nel caso in cui uno studente abbia rifiutato, viene posto a rimandato
            valutazioneDAO.aggiornaVerbalizzato();

            // carico le informazioni complete degli studenti di cui avevo preso l'id in
            // studentiDaAggiornare
            studentiAggiornati = valutazioneDAO.getInfoStudentiAggiornati(appid, studentiDaAggiornare);

            // creo il verbale
            valutazioneDAO.creaVerbale();

            // carica le informazioni relative all'ultimo verbale creato
            verbale = valutazioneDAO.getUltimoVerbale();
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Errore nel recuperare il verbale creato.");
            return;
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Il parametro appId deve essere un intero valido");
            return;
        }

        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
        IWebExchange webExchange = application.buildExchange(request, response);
        WebContext ctx = new WebContext(webExchange, request.getLocale());

        ctx.setVariable("verbale", verbale);
        ctx.setVariable("infoverbalizzati", studentiAggiornati);
        templateEngine.process("/WEB-INF/verbale.html", ctx, response.getWriter());
    }
}