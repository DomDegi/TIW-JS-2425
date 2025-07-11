/**
 * 
 */
package it.polimi.tiw.controllers;

import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import org.thymeleaf.web.IWebExchange;

import it.polimi.tiw.beans.UtenteBean;
import it.polimi.tiw.beans.StudenteAppelloBean;
import it.polimi.tiw.dao.StudenteDAO;
import it.polimi.tiw.utilities.DBConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class esito
 */
@WebServlet("/esito")
public class EsitoStudente extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public EsitoStudente() {
		// TODO Auto-generated constructor stub
	}
	
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		UtenteBean utente = (UtenteBean) request.getSession().getAttribute("utente");
		JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
		IWebExchange webExchange = application.buildExchange(request, response);
		WebContext ctx = new WebContext(webExchange, request.getLocale());
		try {
			String appelloIdParam = request.getParameter("id_appello");
			int id_appello = Integer.parseInt(appelloIdParam);
			StudenteDAO studenteDAO = new StudenteDAO(connection,utente.getIDUtente());
			List<Integer> studenti = studenteDAO.getStudentiIscrittiAppello(id_appello);
			if (!studenti.contains(utente.getIDUtente())) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Non sei iscritto a questo appello");
				return;
			}
			// Caricamento le informazioni dell'appello dello studente
			StudenteAppelloBean infoAppello = studenteDAO.getInfoAppello(id_appello);
			if (infoAppello.getVoto() == null) {
				ctx.setVariable("erroreAppello", "Non ti sei iscritto a questo appello");
				templateEngine.process("/WEB-INF/esito.html", ctx, response.getWriter());
				return;
			}
			ctx.setVariable("infoAppello", infoAppello);
		}
		catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile recuperare l'esito dell'appello");
			return;
		}
		catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "L'id dell'appello non è valido");
			return;
		}
		templateEngine.process("/WEB-INF/esito.html", ctx, response.getWriter());
	}
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		UtenteBean utente = (UtenteBean) request.getSession().getAttribute("utente");
		String appelloIdParam = request.getParameter("id_appello");
		int id_appello = Integer.parseInt(appelloIdParam);

		StudenteDAO studenteDAO = new StudenteDAO(connection, utente.getIDUtente());

		// aggiorno lo stato di valutazione nel database ponendolo a rifiutato
		try {
			studenteDAO.setRifiutato(id_appello);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile rifiutare il voto");
			return;
		}
		doGet(request, response);
	}
	
	

}
