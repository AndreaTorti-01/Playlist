package controllers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Year;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.lang.StringEscapeUtils;

import beans.User;
import dao.SongDAO;
import utils.ConnectionHandler;

@WebServlet("/UploadSong")
@MultipartConfig
public class CreateSong extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection = null;

	private SongDAO songDAO;

	public CreateSong() {
		super();
	}

	@Override
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
		songDAO = new SongDAO(connection);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// If the user is not logged in (not present in session) redirect to the login
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			String loginpath = getServletContext().getContextPath() + "/index.html";
			response.sendRedirect(loginpath);
			return;
		}

		// If the user is logged in correctly then set the user attribute
		User user = (User) session.getAttribute("user");

		// get file parts from the POST request
		Part songFilePart = request.getPart("songFile");
		Part imageFilePart = request.getPart("imageFile");

		// We first check the parameter needed is present
		if (songFilePart == null || songFilePart.getSize() <= 0 || imageFilePart == null
				|| imageFilePart.getSize() <= 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing file in request!");
			return;
		}

		// We then check the parameter is valid (in this case right format)
		// recupera il MIME type della parte "songname"
		// se il tipo di contenuto non è audio, manda messaggio di errore formato file
		String audioType = songFilePart.getContentType();
		String imageType = imageFilePart.getContentType();
		if (!audioType.startsWith("audio/mpeg") || !imageType.startsWith("image")) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Audio or Image file format not permitted");
			return;
		}

		// build the path to save the song file
		// recupera dal web.xml il path in cui salvare le canzoni
		String filesFolder = getServletContext().getInitParameter("outputpath");

		// restituisce il nome dello user tramite il metodo definito nel UserDAO
		String userFolder = user.getUsername();

		// costruisce l'oggetto Path completo di dove mettere il file inviato dallo
		// specifico user
		Path fileFolder = Paths.get(filesFolder + userFolder);

		// create the directory if it does not exist
		Files.createDirectories(fileFolder);

		String fileName = Paths.get(songFilePart.getSubmittedFileName()).getFileName().toString();

		String filePath = Paths.get(filesFolder + userFolder + "/" + fileName).toString();

		// get current year
		int currentYear = Year.now().getValue();

		// get request parameters
		String title = fileName.substring(0, fileName.lastIndexOf('.'));
		String owner = user.getUsername();
		String author = StringEscapeUtils.escapeJava(request.getParameter("author"));
		String album = StringEscapeUtils.escapeJava(request.getParameter("album"));
		String genre = StringEscapeUtils.escapeJava(request.getParameter("genre"));
		int albumYear = Integer.parseInt(request.getParameter("albumYear"));

		// Get the file name
		String imageFileName = album + ".jpg";

		// Get the full file path
		Path imageFilePath = fileFolder.resolve(imageFileName);

		// check if the songs already exists
		try {
			if (songDAO.doesSongExist(title, owner)) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Song already exists");
				return;
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// check if the parameters are bad
		if (title.isEmpty() || author.isEmpty() || album.isEmpty() || genre.isEmpty() || albumYear > currentYear) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to upload song");
			return;
		}

		// Convert the uploaded image to JPG format and save it
		try (InputStream input = imageFilePart.getInputStream()) {
			BufferedImage image = ImageIO.read(input);
			if (image != null) {
				ImageIO.write(image, "jpg", imageFilePath.toFile());
			}
		}

		// save the audio file
		File file = new File(filePath);
		try (InputStream fileContent = songFilePart.getInputStream()) {
			Files.copy(fileContent, file.toPath());
		}

		// upload the other song data
		try {
			songDAO.uploadData(title, owner, author, album, genre, albumYear);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// return the user to the right view
		String ctxpath = getServletContext().getContextPath();
		String path = ctxpath + "/Home";
		response.sendRedirect(path);

	}

	@Override
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}