package it.polimi.tiw.filters;

import java.io.IOException;

import it.polimi.tiw.beans.UtenteBean;
import it.polimi.tiw.beans.DocenteBean;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class DocenteChecker implements Filter {

    public DocenteChecker() {}

    public void destroy() {}

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        boolean isAjax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With")) ||
                         (req.getHeader("Accept") != null && req.getHeader("Accept").contains("application/json"));

        if (session == null) {
            if (isAjax) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\": \"Utente non autenticato\"}");
            } else {
                res.sendRedirect(req.getContextPath() + "/index.html");
            }
            return;
        }

        UtenteBean utente = (UtenteBean) session.getAttribute("utente");
        if (utente == null || !(utente.getRuolo().equals("docente"))) {
            if (isAjax) {
                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\": \"Utente non autorizzato\"}");
            } else {
                res.sendRedirect(req.getContextPath() + "/index.html");
            }
            return;
        }

        System.out.println("Docente autenticato: " + utente.getEmail());
        chain.doFilter(request, response);
    }

    public void init(FilterConfig fConfig) throws ServletException {}
} 