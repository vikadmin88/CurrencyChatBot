package org.javacrafters.core.storage;

import com.google.gson.Gson;
import org.javacrafters.core.AppRegistry;
import org.javacrafters.scheduler.Scheduler;
import org.javacrafters.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * @author Maryna Yeretska, marinka11071979@gmail.com
 */
public class SQLiteStorageProvider implements StorageProvider {
    private static final String STORAGE_FOLDER = "./botusers";
    private static final String JDBC_URL = "jdbc:sqlite:" + STORAGE_FOLDER + "/users.sqlite";
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteStorageProvider.class);
    private static final Gson GSON = new Gson();

    public SQLiteStorageProvider() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL);
             Statement statement = connection.createStatement()) {

            String createTableQuery = "CREATE TABLE IF NOT EXISTS users (id BIGINT PRIMARY KEY, json TEXT)";
            statement.executeUpdate(createTableQuery);
            LOGGER.info("Initialize database {}", JDBC_URL);

        } catch (SQLException e) {
            LOGGER.error("Database {} not found, creating new one.  Method initializeDatabase()", JDBC_URL);
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
                LOGGER.info("User {} loaded from database.", userId);
                return GSON.fromJson(json, User.class);
            }

        } catch (SQLException e) {
            LOGGER.error("Cant connect to database {} Method load(Long userId): {}", JDBC_URL, userId, e);
        }
        return null;
    }

    @Override
    public void load() {
        if (!AppRegistry.getConfIsUsingUsersStorage()) {
            return;
        }
        Runnable taskLoadUsers = new Runnable() {
            public void run() {
                String selectAllQuery = "SELECT id, json FROM users";
                try (Connection connection = DriverManager.getConnection(JDBC_URL)) {

                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(selectAllQuery);

                    while (resultSet.next()) {
                        long userId = resultSet.getLong("id");
                        String userData = resultSet.getString("json");
                        User user = GSON.fromJson(userData, User.class);

                        AppRegistry.addUser(user);
                        Scheduler.addUserSchedule(user.getId(), user, user.getNotifyTime());

                        LOGGER.info("User {} loaded from database Thread: {}", user.getId(), Thread.currentThread().getName());
                    }

                } catch (SQLException e) {
                    LOGGER.error("Cant connect to database {} Method load()", JDBC_URL, e);
                }
            }
        };
        new Thread(taskLoadUsers).start();
    }

    @Override
    public void save(User user) {
        if (!AppRegistry.getConfIsUsingUsersStorage()) {
            return;
        }
        Runnable taskSave = new Runnable() {
            public void run() {
                try (Connection connection = DriverManager.getConnection(JDBC_URL)) {

                     PreparedStatement selectStatement = connection.prepareStatement("SELECT * FROM users WHERE id = ?");
                     PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO users (id, json) VALUES (?, ?)");
                     PreparedStatement updateStatement = connection.prepareStatement("UPDATE users SET json = ? WHERE id = ?");

                    selectStatement.setLong(1, user.getId());
                    ResultSet resultSet = selectStatement.executeQuery();

                    if (resultSet.next()) {
                        updateStatement.setString(1, GSON.toJson(user));
                        updateStatement.setLong(2, user.getId());
                        updateStatement.executeUpdate();
                        LOGGER.info("User {} updated in database. Thread: {}", user.getId(), Thread.currentThread().getName());
                    } else {
                        insertStatement.setLong(1, user.getId());
                        insertStatement.setString(2, GSON.toJson(user));
                        insertStatement.executeUpdate();
                        LOGGER.info("User {} saved to database Thread: {}", user.getId(), Thread.currentThread().getName());
                    }

                } catch (SQLException e) {
                    LOGGER.error("Cant connect to database {} Method save(User user): {}", JDBC_URL, user.getId(), e);
                }
            }
        };
        new Thread(taskSave).start();
    }
    @Override
    public void delete(Long userId) {
        if (!AppRegistry.getConfIsUsingUsersStorage()) {
            return;
        }
        Runnable taskSave = new Runnable() {
            public void run() {
                try (Connection connection = DriverManager.getConnection(JDBC_URL)) {

                    PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM users WHERE id = ?");
                    deleteStatement.setLong(1, userId);
                    deleteStatement.executeUpdate();
                    LOGGER.info("User {} deleted from database Thread: {}", userId, Thread.currentThread().getName());

                } catch (SQLException e) {
                    LOGGER.error("Cant connect to database {} Method delete(Long userId): {}", JDBC_URL, userId, e);
                }
            }
        };
        new Thread(taskSave).start();
    }
}