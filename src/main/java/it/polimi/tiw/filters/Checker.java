package it.polimi.tiw.filters;

import java.io.IOException;


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
 * Filter per l'autenticazione degli utenti
 * Controlla che l'utente sia autenticato prima di accedere alle pagine protette
 */
public class Checker implements Filter {

    /**
     * Default constructor.
     */
    public Checker() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @see Filter#destroy()
     */
    public void destroy() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        System.out.println("Authentication filter executing...");

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        
        // Percorsi pubblici che non richiedono autenticazione
        String loginPath = req.getContextPath() + "/index.html";
        String loginServletPath = req.getContextPath() + "/login";
        String cssPath = req.getContextPath() + "/styles/";
        String jsPath = req.getContextPath() + "/js/";
        String imagesPath = req.getContextPath() + "/images/";
        
        String requestURI = req.getRequestURI();
        
        // Controlla se la richiesta è per una risorsa pubblica
        boolean isPublicResource = requestURI.equals(loginPath) || 
                                   requestURI.equals(loginServletPath) ||
                                   requestURI.startsWith(cssPath) ||
                                   requestURI.startsWith(jsPath) ||
                                   requestURI.startsWith(imagesPath) ||
                                   requestURI.endsWith(".css") ||
                                   requestURI.endsWith(".js") ||
                                   requestURI.endsWith(".png") ||
                                   requestURI.endsWith(".jpg") ||
                                   requestURI.endsWith(".jpeg") ||
                                   requestURI.endsWith(".gif") ||
                                   requestURI.endsWith(".ico");
        
        // Se è una risorsa pubblica, lascia passare
        if (isPublicResource) {
            chain.doFilter(request, response);
            return;
        }
        
        // Controlla se l'utente è autenticato
        if (session == null || session.getAttribute("utente") == null) {
            System.out.println("Utente non autenticato, verrà reinderizzato al login");
            res.sendRedirect(loginPath);
            return;
        }
        
        // Utente autenticato, continua con la richiesta
        System.out.println("Utente autenticato: " + session.getAttribute("utente"));
        chain.doFilter(request, response);
    }

    /**
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig fConfig) throws ServletException {
        // TODO Auto-generated constructor stub
    }
} 