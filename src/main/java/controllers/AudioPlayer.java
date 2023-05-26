package controllers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/audioPlayer/*")
public class AudioPlayer extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public AudioPlayer() {
		super();
		// TODO Auto-generated constructor stub
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

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
