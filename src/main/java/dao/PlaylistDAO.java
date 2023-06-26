package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDAO {
    private final Connection connection;

    public PlaylistDAO(Connection connection) {
        this.connection = connection;
    }

    public void createPlaylist(String playlistOwner, List<String> playlistSongs, String playlistName, List<Integer> albumYears, String creationDate) throws SQLException {
        connection.setAutoCommit(false); // transaction block start

        String query = "INSERT INTO playlist (playlistOwner, playlistSong, playlistName, albumYear, creationDate, songIndexJs) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = null;

        try {

            // insert every song in the playlist
            for (int i = 0; i < playlistSongs.size(); i++) {
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, playlistOwner);
                preparedStatement.setString(2, playlistSongs.get(i));
                preparedStatement.setString(3, playlistName);
                preparedStatement.setInt(4, albumYears.get(i));
                preparedStatement.setString(5, creationDate);
                preparedStatement.setInt(6, i);

                preparedStatement.executeUpdate();

                connection.commit();
            }

        } catch (SQLException e) {
            connection.rollback(); // transaction block abort
            System.out.println("Errore nella creazione della playlist");
        } finally {
            connection.setAutoCommit(true); // transaction block end
            if (preparedStatement != null)
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }

    }

    public List<String> getPlaylistsOf(String playlistOwner) throws SQLException {
        String query = "SELECT DISTINCT playlistName, creationDate FROM playlist WHERE playlistOwner = ? ORDER BY creationDate DESC";
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        preparedStatement.setString(1, playlistOwner);

        ResultSet res = preparedStatement.executeQuery();

        List<String> ret = new ArrayList<>();
        String name;
        while (res.next()) {
            name = res.getString("playlistName");
            ret.add(name);
        }

        return ret;
    }

    public int getSongsNumOfPlaylistOf(String playlistOwner, String playlistName) throws SQLException {
        String query = "SELECT COUNT(*) playlistSong FROM playlist WHERE playlistOwner = ? AND playlistName = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, playlistOwner);
        preparedStatement.setString(2, playlistName);

        ResultSet res = preparedStatement.executeQuery();
        int num = 0;
        while (res.next()) {
            num = res.getInt(1);
        }

        return num;
    }

    public List<String> getSongsOfPlaylistOf(String playlistOwner, String playlistName) throws SQLException {
        String query = "SELECT playlistSong, albumYear FROM playlist WHERE playlistOwner = ? AND playlistName = ? ORDER BY albumYear DESC";
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        preparedStatement.setString(1, playlistOwner);
        preparedStatement.setString(2, playlistName);

        ResultSet res = preparedStatement.executeQuery();

        List<String> ret = new ArrayList<>();
        String name;
        while (res.next()) {
            name = res.getString("playlistSong");
            ret.add(name);
        }

        return ret;
    }

    public List<String> getFiveSongsAtMost(String playlistOwner, String playlistName, int offset) throws SQLException {
        String query = "SELECT playlistSong, albumYear FROM playlist WHERE playlistOwner = ? AND playlistName = ? ORDER BY albumYear DESC LIMIT 5 OFFSET ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        preparedStatement.setString(1, playlistOwner);
        preparedStatement.setString(2, playlistName);
        preparedStatement.setInt(3, offset);

        ResultSet res = preparedStatement.executeQuery();
        List<String> ret = new ArrayList<>();
        String name;

        while (res.next()) {
            name = res.getString("playlistSong");
            ret.add(name);
        }
        return ret;
    }

    public int getPlaylistNextFreeIndex(String playlistOwner, String playlistName) throws SQLException {
        String query = "SELECT MAX(songIndexJs) FROM playlist WHERE playlistOwner = ? AND playlistName = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, playlistOwner);
        preparedStatement.setString(2, playlistName);

        ResultSet res = preparedStatement.executeQuery();
        int index = 0;
        while (res.next()) {
            index = res.getInt(1);
        }

        return index + 1;
    }

    public String getPlaylistDate(String playlistOwner, String playlistName) throws SQLException {
        String query = "SELECT creationDate FROM playlist WHERE playlistOwner = ? AND playlistName = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, playlistOwner);
        preparedStatement.setString(2, playlistName);

        ResultSet res = preparedStatement.executeQuery();
        String date = "";
        while (res.next()) {
            date = res.getString(1);
        }

        return date;
    }

    public void addSong(String username, String playlistName, String newSong, int albumYear) throws SQLException {
        String query = "INSERT INTO playlist (playlistOwner, playlistSong, playlistName, albumYear, creationDate, songIndexJs) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        preparedStatement.setString(1, username);
        preparedStatement.setString(2, newSong);
        preparedStatement.setString(3, playlistName);
        preparedStatement.setInt(4, albumYear);
        preparedStatement.setString(5, getPlaylistDate(username, playlistName));
        preparedStatement.setInt(6, getPlaylistNextFreeIndex(username, playlistName));

        preparedStatement.executeUpdate();
    }
}
