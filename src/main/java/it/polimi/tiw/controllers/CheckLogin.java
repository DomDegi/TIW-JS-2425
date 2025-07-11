package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.beans.UtenteBean;
import it.polimi.tiw.dao.UtenteDAO;
import it.polimi.tiw.utilities.DBConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class CheckLogin extends HttpServlet {
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
        // Imposta la directory dei template se necessario, es: templateResolver.setPrefix("/templates/");
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Mostra la pagina di login
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
        IWebExchange webExchange = application.buildExchange(request, response);
        WebContext ctx = new WebContext(webExchange, request.getLocale());
        ctx.setVariable("errorMsg", null);
        templateEngine.process("index.html", ctx, response.getWriter());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
            IWebExchange webExchange = application.buildExchange(request, response);
            WebContext ctx = new WebContext(webExchange, request.getLocale());
            ctx.setVariable("errorMsg", "Email e password obbligatorie");
            templateEngine.process("index.html", ctx, response.getWriter());
            return;
        }

        UtenteDAO utenteDAO = new UtenteDAO(connection);
        UtenteBean utente = null;
        try {
            utente = utenteDAO.checkCredentials(email, password);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
            IWebExchange webExchange = application.buildExchange(request, response);
            WebContext ctx = new WebContext(webExchange, request.getLocale());
            ctx.setVariable("errorMsg", "Errore interno, riprova più tardi");
            templateEngine.process("index.html", ctx, response.getWriter());
            return;
        }

        if (utente == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
            IWebExchange webExchange = application.buildExchange(request, response);
            WebContext ctx = new WebContext(webExchange, request.getLocale());
            ctx.setVariable("errorMsg", "Email o password errati");
            templateEngine.process("index.html", ctx, response.getWriter());
        } else {
            request.getSession().setAttribute("utente", utente);
            String target = (utente instanceof it.polimi.tiw.beans.StudenteBean)
                    ? "/home-studente"
                    : "/home-docente";
            response.sendRedirect(getServletContext().getContextPath() + target);
        }
    }

    public void destroy() {
        try {
            DBConnection.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
