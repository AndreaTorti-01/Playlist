package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import beans.Song;
import beans.User;
import dao.SongDAO;
import utils.ConnectionHandler;

@WebServlet("/playerPage/*")
public class GetPlayerPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;
	private SongDAO songDAO;

	public GetPlayerPage() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		connection = ConnectionHandler.getConnection(getServletContext());
		songDAO = new SongDAO(connection);
	}

	@Override
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

		String userAndSong = request.getPathInfo().substring(1); // Extract the file name from the request URL
		String songName = userAndSong.substring(userAndSong.indexOf('/') + 1);

		Song song = null;
		try {
			song = songDAO.getSongDetails(user.getUsername(), songName);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

		ctx.setVariable("userAndSong", userAndSong);
		ctx.setVariable("songTitle", song.getTitle());
		ctx.setVariable("songAuthor", song.getAuthorName());
		ctx.setVariable("songAlbum", song.getAlbumName());
		ctx.setVariable("songGenre", song.getGenre());
		ctx.setVariable("songAlbumYear", song.getAlbumYear());

		String path = "/WEB-INF/PlayerPage.html";

		// render page
		templateEngine.process(path, ctx, response.getWriter());

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
