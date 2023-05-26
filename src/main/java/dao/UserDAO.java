package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import beans.User;

public class UserDAO {
	private Connection connection;

	public UserDAO(Connection connection) {
		this.connection = connection;
	}

	public User checkCredentials(String usrn, String pwd) throws SQLException {
		String query = "SELECT  nome, cognome, username FROM users WHERE username = ? AND password = ?";
		PreparedStatement pstatement = connection.prepareStatement(query);
		
		pstatement.setString(1, usrn);
		pstatement.setString(2, pwd);
		ResultSet result = pstatement.executeQuery();
		
		if (!result.isBeforeFirst()) // no results, credential check failed, si usa la negazione ! in quanto
			return null;			//indica che nel resultSet non ci sono righe 
		
		else {
			result.next();
			User user = new User(); //da qua in poi costruiamo l'oggetto user con i relativi campi recuperati tramite la query sopra
			user.setNome(result.getString("nome")); //nome indica la label della colonna relativa alla riga corrente del resultSet, di cui andiamo a recuperare il valore (String) corrispondente al nome dell'user, che sarà assegnato nel metodo setNome di User.java
			user.setCognome(result.getString("cognome")); //analogo a sopra
			user.setUsername(result.getString("username")); //analogo a sopra
			return user;
		}
	}
}
