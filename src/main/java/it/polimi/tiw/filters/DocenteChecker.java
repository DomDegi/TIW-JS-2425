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

/**
 * Filter per l'autorizzazione dei docenti
 * Consente l'accesso solo se l'utente in sessione è un DocenteBean
 */
public class DocenteChecker implements Filter {

    public DocenteChecker() {
        // Costruttore di default
    }

    public void destroy() {
        // Nessuna risorsa da liberare
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        if (session == null) {
            res.sendRedirect(req.getContextPath() + "/index.html");
            return;
        }

        UtenteBean utente = (UtenteBean) session.getAttribute("utente");
        if (utente == null || !(utente instanceof DocenteBean)) {
            System.out.println("L'utente non è un docente, accesso negato");
            res.sendRedirect(req.getContextPath() + "/index.html");
            return;
        }

        // Qui sei sicuro che è un docente autenticato
        System.out.println("Docente autenticato: " + utente.getEmail());
        chain.doFilter(request, response);
    }

    public void init(FilterConfig fConfig) throws ServletException {
        // Nessuna inizializzazione necessaria
    }
} 