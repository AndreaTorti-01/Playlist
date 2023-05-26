package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import utils.ConnectionHandler;
import beans.User;
import dao.PlaylistDAO;

/**
 * Servlet implementation class GetPlaylistDetails
 */
@WebServlet("/GetPlaylistDetails")
public class GetPlaylistDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	private PlaylistDAO playlistDAO;
	int numberOfSongs;

	public GetPlaylistDetails() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		connection = ConnectionHandler.getConnection(getServletContext());
		playlistDAO = new PlaylistDAO(connection);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// If the user is not logged in (not present in session) redirect to the login
		String loginpath = getServletContext().getContextPath() + "/index.html";
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		}

		// If the user is logged in correctly then set the user attribute
		User user = (User) session.getAttribute("user");
		String playlistName;
		int page = Integer.parseInt(request.getParameter("page"));
		// Set the offset as the page*5
		int offset = page * 5;
		
		playlistName = StringEscapeUtils.escapeJava(request.getParameter("id"));
		
		int lastPage = 0;
		// Gets number of songs
		try {
			lastPage = playlistDAO.getSongsNumOfPlaylistOf(user.getUsername(), playlistName) / 5;
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		List<String> playlistSongs = null;
		try {
			playlistSongs = playlistDAO.getFiveSongsAtMost(user.getUsername(), playlistName, offset);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		request.setAttribute("playlistSongs", playlistSongs);
		
		request.setAttribute("page", page);
		
		request.setAttribute("id", playlistName);
		
		request.setAttribute("lastPage", lastPage);
		
		try {
			numberOfSongs = playlistDAO.getSongsNumOfPlaylistOf(user.getUsername(), playlistName);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("username", user.getUsername());
		ctx.setVariable("numberOfSongs", numberOfSongs);
		
		String path = "/WEB-INF/PlaylistDetails.html";
	
		// render page
		templateEngine.process(path, ctx, response.getWriter());

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
