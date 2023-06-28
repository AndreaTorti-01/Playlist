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
import java.util.ArrayList;
import java.util.List;

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
        // get the selected song as the newSong request parameter
        String newSong = StringEscapeUtils.escapeJava(request.getParameter("newSong"));

        if (playlistName.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No name given");
            return;
        }

        // get number of songs in the playlist
        int numberOfSongs;
        try {
            numberOfSongs = playlistDAO.getSongsNumOfPlaylistOf(user.getUsername(), playlistName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // if the playlist is empty then use createPlaylist dao method
        if (numberOfSongs == 0 && !newSong.isEmpty()) {
            List<String> singleSongList = new ArrayList<>();
            List<Integer> singleAlbumYearList = new ArrayList<>();
            try {

                singleSongList.add(newSong);
                singleAlbumYearList.add(songDAO.getSongDetails(user.getUsername(), newSong).getAlbumYear());

                java.util.Date dt = new java.util.Date();

                java.text.SimpleDateFormat sdf =
                        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                String currentTime = sdf.format(dt);

                playlistDAO.createPlaylist(user.getUsername(), singleSongList, playlistName, singleAlbumYearList, currentTime);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (!newSong.isEmpty()) {
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
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No songs selected");
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
