package org.javacrafters.core.storage;

import com.google.gson.Gson;
import org.javacrafters.core.AppRegistry;
import org.javacrafters.scheduler.Scheduler;
import org.javacrafters.user.User;

import java.sql.*;

public class SQLiteStorageProvider implements StorageProvider {
    private static final String JDBC_URL = "jdbc:sqlite:база_даних.db";

    public SQLiteStorageProvider() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL);
             Statement statement = connection.createStatement()) {

            String createTableQuery = "CREATE TABLE  users (id BIGINT PRIMARY KEY, json TEXT)";
            statement.executeUpdate(createTableQuery);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public User load(Long userId) {
        String selectJsonQuery = "SELECT json FROM users WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(JDBC_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(selectJsonQuery)) {

            preparedStatement.setLong(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String json = resultSet.getString("json");
                return new Gson().fromJson(json, User.class);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void load() {
        String selectAllQuery = "SELECT id, json FROM users";
        try (Connection connection = DriverManager.getConnection(JDBC_URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectAllQuery)) {

            while (resultSet.next()) {
                long userId = resultSet.getLong("id");
                String userData = resultSet.getString("json");
                User user = new Gson().fromJson(userData, User.class);

                AppRegistry.addUser(user);
                Scheduler.addUserSchedule(user.getId(), user, user.getNotifyTime());

                System.out.printf("User %d loaded from database.\n", user.getId());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(User user) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL);
             PreparedStatement selectStatement = connection.prepareStatement("SELECT * FROM users WHERE id = ?");
             PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO users (id, json) VALUES (?, ?)");
             PreparedStatement updateStatement = connection.prepareStatement("UPDATE users SET json = ? WHERE id = ?")) {

            selectStatement.setLong(1, user.getId());
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                updateStatement.setString(1, new Gson().toJson(user));
                updateStatement.setLong(2, user.getId());
                updateStatement.executeUpdate();
            } else {
                insertStatement.setLong(1, user.getId());
                insertStatement.setString(2, new Gson().toJson(user));
                insertStatement.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}