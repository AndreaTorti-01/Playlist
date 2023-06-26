package controllers;

import beans.User;
import dao.PlaylistDAO;
import dao.SongDAO;
import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import utils.ConnectionHandler;

import javax.servlet.ServletContext;
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

/**
 * Servlet implementation class GetPlaylistDetails
 */
@WebServlet("/GetPlaylistDetails")
public class GetPlaylistDetails extends HttpServlet {
    int numberOfSongs;
    private TemplateEngine templateEngine;
    private PlaylistDAO playlistDAO;
    private SongDAO songDAO;

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
        Connection connection = ConnectionHandler.getConnection(getServletContext());
        playlistDAO = new PlaylistDAO(connection);
        songDAO = new SongDAO(connection);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // If the user is logged in correctly then set the user attribute
        User user = (User) request.getSession().getAttribute("user");
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
            e1.printStackTrace();
        }
        List<String> playlistSongs = null;
        try {
            playlistSongs = playlistDAO.getFiveSongsAtMost(user.getUsername(), playlistName, offset);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // now get the album name for each song
        List<String> albumNames = new ArrayList<>();
        assert playlistSongs != null;
        for (String ps : playlistSongs) {
            try {
                albumNames.add(songDAO.getSongDetails(user.getUsername(), ps).getAlbumName());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        request.setAttribute("playlistSongs", playlistSongs);

        request.setAttribute("page", page);

        request.setAttribute("id", playlistName);

        request.setAttribute("lastPage", lastPage);

        request.setAttribute("albumNames", albumNames);

        try {
            numberOfSongs = playlistDAO.getSongsNumOfPlaylistOf(user.getUsername(), playlistName);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<String> allUserSongs = null;
        try {
            allUserSongs = songDAO.getSongsOf(user.getUsername());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("allUserSongs", allUserSongs);

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("username", user.getUsername());
        ctx.setVariable("numberOfSongs", numberOfSongs);

        String path = "/WEB-INF/PlaylistDetails.html";

        // render page
        templateEngine.process(path, ctx, response.getWriter());

    }

}
