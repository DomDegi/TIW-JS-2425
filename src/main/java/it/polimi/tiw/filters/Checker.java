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


public class Checker implements Filter {

    /**
     * Default constructor.
     */
    public Checker() {}

    /**
     * @see Filter#destroy()
     */
    public void destroy() {}

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        boolean isAjax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With")) ||
                         (req.getHeader("Accept") != null && req.getHeader("Accept").contains("application/json"));

        if (session == null || session.getAttribute("utente") == null) {
            if (isAjax) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\": \"Utente non autenticato\"}");
            } else {
                res.sendRedirect(req.getContextPath() + "/index.html");
            }
            return;
        }
        chain.doFilter(request, response);
    }

    /**
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig fConfig) throws ServletException {}
} 