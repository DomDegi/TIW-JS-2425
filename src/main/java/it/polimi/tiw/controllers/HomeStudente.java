package it.polimi.tiw.controllers;
import it.polimi.tiw.beans.CorsoBean;
import it.polimi.tiw.utilities.DBConnection;
import it.polimi.tiw.beans.AppelloBean;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.sql.*;
import java.util.List;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.beans.UtenteBean;
import it.polimi.tiw.dao.StudenteDAO;

@WebServlet("/home-studente")
public class HomeStudente extends HttpServlet {
	
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;
    
    
    @Override
    public void init() throws ServletException {
        this.connection = DBConnection.getConnection(getServletContext());
        ServletContext servletContext = getServletContext();

        JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(servletContext);
        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(webApplication);

        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setSuffix(".html");
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UtenteBean utente = (UtenteBean) request.getSession().getAttribute("utente");
        if (utente == null) {
            response.sendRedirect(getServletContext().getContextPath() + "/login");
            return;
        }

        StudenteDAO studenteDAO = new StudenteDAO(connection, utente.getIDUtente());

        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
        IWebExchange webExchange = application.buildExchange(request, response);
        WebContext ctx = new WebContext(webExchange, request.getLocale());
        ctx.setVariable("email", utente.getEmail());

        try {
            List<CorsoBean> corsi = studenteDAO.cercaCorsi();
            ctx.setVariable("corsi", corsi);

            String corsoIdParam = request.getParameter("corsoId");
            if (corsoIdParam != null) {
                int corsoId = Integer.parseInt(corsoIdParam);
                List<Integer> studenti = studenteDAO.getStudentiIscrittiCorso(corsoId);
                if (!studenti.contains(utente.getIDUtente())) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Non sei iscritto a questo corso");
                    return;
                }
                List<AppelloBean> appelli = studenteDAO.cercaAppelliStudente(corsoId);
                ctx.setVariable("appelli", appelli);

                CorsoBean corsoSelezionato = null;
            for (CorsoBean c : corsi) {
                if (c.getIDCorso() == corsoId) {
                    corsoSelezionato = c;
                    break;
                }
            }
            ctx.setVariable("corsoSelezionato", corsoSelezionato);
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile recuperare i dati");
            return;
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Il parametro corsoId deve essere un intero valido");
            return;
        }
        templateEngine.process("/WEB-INF/home-studente.html", ctx, response.getWriter());
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}

