package controllers;

import beans.User;
import dao.PlaylistDAO;
import dao.SongDAO;
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
import java.time.LocalDate;
import java.util.List;

@WebServlet("/Home")
public class GoToHomePage extends HttpServlet {
    private TemplateEngine templateEngine;
    private Connection connection = null;

    public GoToHomePage() {
        super();
    }

    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String path = "/WEB-INF/Home.html";
        ServletContext servletContext = getServletContext();

        // get list of user songs
        List<String> userSongs = null;
        User user = (User) request.getSession().getAttribute("user");
        try {
            userSongs = new SongDAO(connection).getSongsOf(user.getUsername());
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover songs");
        }
        request.setAttribute("userSongs", userSongs);

        // get list of user playlists
        List<String> userPlaylists = null;
        try {
            userPlaylists = new PlaylistDAO(connection).getPlaylistsOf(user.getUsername());
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover playlists");
        }
        request.setAttribute("userPlaylists", userPlaylists);

        // get current year
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        // add it to the request, without putting it in the session: this is for THYMELEAF
        request.setAttribute("currentYear", currentYear);
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

        // render page
        templateEngine.process(path, ctx, response.getWriter());
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
