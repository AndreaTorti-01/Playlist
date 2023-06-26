package dao;

import beans.Song;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SongDAO {
    private final Connection connection;

    public SongDAO(Connection connection) {
        this.connection = connection;
    }

    public void uploadData(String title, String owner, String author, String album, String genre, int albumYear)
            throws SQLException {
        String songQuery = "INSERT into songs (title, owner, author, album, genre, albumYear) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(songQuery);

        preparedStatement.setString(1, title);
        preparedStatement.setString(2, owner);
        preparedStatement.setString(3, author);
        preparedStatement.setString(4, album);
        preparedStatement.setString(5, genre);
        preparedStatement.setInt(6, albumYear);

        preparedStatement.executeUpdate();
    }

    public boolean doesSongExist(String title, String owner) throws SQLException {
        String songQuery = "SELECT * FROM songs WHERE title = ? and owner = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(songQuery);

        preparedStatement.setString(1, title);
        preparedStatement.setString(2, owner);

        ResultSet songsWithThisTitle = preparedStatement.executeQuery();

        // no results, credential check failed, si usa la negazione ! in quanto indica
        // che nel resultSet non ci sono righe
        return songsWithThisTitle.isBeforeFirst();
    }

    public List<String> getSongsOf(String owner) throws SQLException {
        String songQuery = "SELECT title FROM songs WHERE owner = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(songQuery);

        preparedStatement.setString(1, owner);

        ResultSet res = preparedStatement.executeQuery();

        List<String> ret = new ArrayList<>();

        String songTitle;
        while (res.next()) {
            songTitle = res.getString("title");
            ret.add(songTitle);
        }

        return ret;
    }

    public Song getSongDetails(String owner, String title) throws SQLException {
        String songQuery = "SELECT title, author, album, genre, albumYear FROM songs WHERE owner = ? AND title = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(songQuery);

        preparedStatement.setString(1, owner);
        preparedStatement.setString(2, title);

        ResultSet res = preparedStatement.executeQuery();
        Song song = new Song();

        if (res.next()) {
            song.setTitle(res.getString("title"));
            song.setAuthorName(res.getString("author"));
            song.setAlbumName(res.getString("album"));
            song.setGenre(res.getString("genre"));
            song.setAlbumYear(res.getInt("albumYear"));
        } else {
            System.out.println("no song found! " + title + " " + owner);
            return null;
        }

        return song;
    }

}
