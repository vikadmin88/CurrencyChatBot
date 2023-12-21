package org.javacrafters.core.storage;

import org.javacrafters.user.User;

import java.sql.*;

public class SQLiteStorageProvider implements StorageProvider {
    private Connection connection;

    public SQLiteStorageProvider(String dbUrl) {
        try {
            connectToDatabase(dbUrl);
            createTableIfNotExists();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void connectToDatabase(String dbUrl) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbUrl);
    }

    private void createTableIfNotExists() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS users (id BIGINT PRIMARY KEY, name TEXT)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(createTableQuery)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save (User user) {
        String saveUserQuery = "INSERT INTO users (id, name) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(saveUserQuery)) {
            preparedStatement.setLong(1, user.getId());
            preparedStatement.setString(2, user.getName());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load() {

    }

    @Override
    public User load (Long userId){
        String getUserQuery = "SELECT * FROM users WHERE id = ?";
       try (PreparedStatement preparedStatement = connection.prepareStatement(getUserQuery)) {
           preparedStatement.setLong(1, userId);
           ResultSet resultSet = preparedStatement.executeQuery();
           if (resultSet.next()) {
               return new User(resultSet.getLong("id"), resultSet.getString("name"));

           }
       }catch (SQLException e) {
           e.printStackTrace();
       }
       return null;
       }


}



