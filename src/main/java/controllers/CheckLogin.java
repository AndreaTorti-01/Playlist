package controllers;

import beans.User;
import dao.UserDAO;
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

@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet {
    UserDAO userDao;
    private Connection connection = null;
    private TemplateEngine templateEngine;

    public CheckLogin() {
        super();
    }

    public void init() throws ServletException {
        // get servlet context
        ServletContext servletContext = getServletContext();
        // connect to database
        connection = ConnectionHandler.getConnection(servletContext);
        // get the template resolver and give it to the template engine (thymeleaf)
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
        userDao = new UserDAO(connection);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // obtain and escape params
        String usrn;
        String pwd;

        try {
            usrn = StringEscapeUtils.escapeJava(request.getParameter("username"));
            pwd = StringEscapeUtils.escapeJava(request.getParameter("password"));
            // check che manchino dei campi
            if (usrn == null || pwd == null || usrn.isEmpty() || pwd.isEmpty()) {
                throw new Exception("Missing or empty credential value");
            }

        } catch (Exception e) {
            // for debugging only e.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing credential value");
            return;
        }

        // query db to authenticate for user
        User user;
        try {
            // assegna all'oggetto user i campi recuperati eseguendo la query tramite il DAO
            user = userDao.checkCredentials(usrn, pwd);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to check credentials");
            return;
        }

        // If the user exists, add info to the session and go to home page, otherwise
        // show login page with error message

        String path;
        if (user == null) {
            // render login error page
            ServletContext servletContext = getServletContext();
            final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
            ctx.setVariable("errorMsg", "Incorrect Username or Password");
            path = "/index.html";
            templateEngine.process(path, ctx, response.getWriter());
        } else {
            // assegno user come attributo "user" dell'oggetto sessione
            request.getSession().setAttribute("user", user);
            // vai alla home
            path = getServletContext().getContextPath() + "/Home";
            response.sendRedirect(path);
        }

    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}