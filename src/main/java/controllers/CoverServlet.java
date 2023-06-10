package controllers;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;

@WebServlet("/cover/*")
public class CoverServlet extends HttpServlet {

    public CoverServlet() {
        super();
    }
    @Override
    public void init() throws ServletException {
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

        String imageFileName = request.getPathInfo().substring(1); // Extract the file name from the request URL
        String audioFilePath = "C:/userSongs/" + imageFileName;

        File imageFile = new File(audioFilePath);

        if (imageFile.exists()) {
            byte[] imageData;

            try (FileInputStream fileInputStream = new FileInputStream(imageFile);
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }

                imageData = byteArrayOutputStream.toByteArray();
            }

            response.setContentType("image/jpeg");
            response.setContentLength(imageData.length);

            try (OutputStream outputStream = response.getOutputStream()) {
                outputStream.write(imageData);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
