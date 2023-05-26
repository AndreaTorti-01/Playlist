package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;

import beans.User;
import dao.PlaylistDAO;
import utils.ConnectionHandler;

/**
 * Servlet implementation class CreatePlaylist
 */
@WebServlet("/CreatePlaylist")
public class CreatePlaylist extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private PlaylistDAO playlistDAO;

	public CreatePlaylist() {
		super();

	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
		playlistDAO = new PlaylistDAO(connection);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		connection = ConnectionHandler.getConnection(getServletContext());

		// If the user is not logged in (not present in session) redirect to the login
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			String loginpath = getServletContext().getContextPath() + "/index.html";
			response.sendRedirect(loginpath);
			return;
		}

		// If the user is logged in correctly then set the user attribute
		User user = (User) session.getAttribute("user");

		// get the playlist name from the post request
		String playlistName = StringEscapeUtils.escapeJava(request.getParameter("playlistName"));

		if (playlistName.isEmpty()) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No name given");
			return;
		}

		// get the list of selected songs
		String[] selectedSongs = request.getParameterValues("checkbox");
		if (selectedSongs != null) {
			// for every song, add it to the playlist
			for (String song : selectedSongs) {
				try {
					playlistDAO.createPlaylist(user.getUsername(), song, playlistName);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else {
			// no songs selected
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No songs selected");
			return;
		}

		// return the user to the right view
		String ctxpath = getServletContext().getContextPath();
		String path = ctxpath + "/Home";
		response.sendRedirect(path);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
