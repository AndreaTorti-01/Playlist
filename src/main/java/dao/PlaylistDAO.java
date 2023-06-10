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

	public void addSong(String playlistOwner, String playlistSong, String playlistName, int albumYear, String creationDate)
			throws SQLException {
		String query = "INSERT INTO playlist (playlistOwner, playlistSong, playlistName, albumYear, creationDate) VALUES (?, ?, ?, ?, ?)";
		PreparedStatement preparedStatement = connection.prepareStatement(query);

		preparedStatement.setString(1, playlistOwner);
		preparedStatement.setString(2, playlistSong);
		preparedStatement.setString(3, playlistName);
		preparedStatement.setInt(4, albumYear);
		preparedStatement.setString(5, creationDate);

		preparedStatement.executeUpdate();
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
	
	public List<String> getSongsOfPlaylistOf(String playlistOwner, String playlistName) throws SQLException {
		String query = "SELECT playlistSong FROM playlist WHERE playlistOwner = ? AND playlistName = ? ORDER BY albumYear DESC";
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
	
	public int getSongsNumOfPlaylistOf (String playlistOwner, String playlistName) throws SQLException {
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
	
	public List<String> getFiveSongsAtMost (String playlistOwner, String playlistName, int offset) throws SQLException{
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
	
}
