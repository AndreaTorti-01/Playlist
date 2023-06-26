package controllers;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@WebServlet("/audioPlayer/*")
public class AudioPlayer extends HttpServlet {

    public AudioPlayer() {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String audioFileName = request.getPathInfo().substring(1); // Extract the file name from the request URL
        String audioFilePath = "C:/userSongs/" + audioFileName;

        File audioFile = new File(audioFilePath);

        if (audioFile.exists()) {
            byte[] audioData;

            try (FileInputStream fileInputStream = new FileInputStream(audioFile);
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }

                audioData = byteArrayOutputStream.toByteArray();
            }

            response.setContentType("audio/mpeg"); // Set the appropriate content type based on your audio file format
            response.setContentLength(audioData.length);

            try (OutputStream outputStream = response.getOutputStream()) {
                outputStream.write(audioData);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

    }
}
