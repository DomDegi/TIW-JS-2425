package it.polimi.tiw.controllers;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

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

import it.polimi.tiw.beans.AppelloBean;
import it.polimi.tiw.beans.CorsoBean;
import it.polimi.tiw.beans.DocenteBean;
import it.polimi.tiw.dao.CorsoDAO;
import it.polimi.tiw.dao.DocenteDAO;
import it.polimi.tiw.utilities.DBConnection;

@WebServlet("/home-docente")
public class HomeDocente extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;

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

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        DocenteBean docente = (DocenteBean) request.getSession().getAttribute("utente");
        if (docente == null) {
            response.sendRedirect(request.getContextPath() + "/index.html");
            return;
        }
        DocenteDAO docenteDAO = new DocenteDAO(connection, docente.getIDUtente());

        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
        IWebExchange webExchange = application.buildExchange(request, response);
        WebContext ctx = new WebContext(webExchange, request.getLocale());
        ctx.setVariable("email", docente.getEmail());

        try {
            List<CorsoBean> corsi = docenteDAO.cercaCorsi();
            ctx.setVariable("corsi", corsi);
            String id_corsoParam = request.getParameter("id_corso");
            if (id_corsoParam != null) {
                int id_corso = Integer.parseInt(id_corsoParam);
                CorsoDAO corsoDAO = new CorsoDAO(connection, id_corso);
                int docenteCorretto = corsoDAO.cercaIdDocentePerCorso();
                if (docenteCorretto != docente.getIDUtente()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Il corso a cui vuoi accedere non è tuo");
                    return;
                }
                List<AppelloBean> appelli = corsoDAO.cercaAppelli();
                ctx.setVariable("appelli", appelli);
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile recuperare i corsi");
            return;
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Il parametro corsoId deve essere un intero valido");
            return;
        }
        templateEngine.process("/WEB-INF/home-docente.html", ctx, response.getWriter());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
} 