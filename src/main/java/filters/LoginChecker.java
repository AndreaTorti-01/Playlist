package filters;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class LoginChecker implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.print("Login checker filter executing ...\n");

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String loginPath = "index.html";

        // Exclude index.html from filtering
        String reqUrl = req.getRequestURI();
        if (reqUrl.endsWith(loginPath)
                || reqUrl.endsWith("css/app.css")
                || reqUrl.endsWith("favicon.ico")
                || reqUrl.endsWith(".js")
                || reqUrl.endsWith("CheckLogin")
                || reqUrl.endsWith("Playlist/")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession s = req.getSession();
        if (s.isNew() || s.getAttribute("user") == null) {
            res.sendRedirect(loginPath);
            System.out.print("Login checker FAILED...\n");
            return;
        }

        chain.doFilter(request, response);
    }

}