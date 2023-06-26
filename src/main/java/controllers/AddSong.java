package controllers;

import beans.User;
import dao.PlaylistDAO;
import dao.SongDAO;
import org.apache.commons.lang.StringEscapeUtils;
import utils.ConnectionHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/AddSong")
public class AddSong extends HttpServlet {
    private Connection connection = null;
    private PlaylistDAO playlistDAO;
    private SongDAO songDAO;

    public AddSong() {
        super();
    }

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
        playlistDAO = new PlaylistDAO(connection);
        songDAO = new SongDAO(connection);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // If the user is logged in correctly then set the user attribute
        User user = (User) request.getSession().getAttribute("user");

        // get the playlist name from the request url parameter called id
        String playlistName = StringEscapeUtils.escapeJava(request.getParameter("id"));

        if (playlistName.isEmpty()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No name given");
            return;
        }

        // get the selected song as the newSong request parameter
        String newSong = StringEscapeUtils.escapeJava(request.getParameter("newSong"));
        if (!newSong.isEmpty()) {
            int albumYear;
            try {
                albumYear = songDAO.getSongDetails(user.getUsername(), newSong).getAlbumYear();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // add to the playlist if not already present
            try {
                if (!playlistDAO.getSongsOfPlaylistOf(user.getUsername(), playlistName).contains(newSong))
                    playlistDAO.addSong(user.getUsername(), playlistName, newSong, albumYear);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // no songs selected
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No songs selected");
            return;
        }

        // return the user to the view he came from
        String path = getServletContext().getContextPath() + "/GetPlaylistDetails?id=" + playlistName + "&page=0";
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
