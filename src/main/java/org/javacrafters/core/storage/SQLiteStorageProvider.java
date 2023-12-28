package org.javacrafters.core.storage;

import com.google.gson.Gson;
import org.javacrafters.core.AppRegistry;
import org.javacrafters.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.sql.*;

/**
 * @author Maryna Yeretska, marinka11071979@gmail.com
 */
public class SQLiteStorageProvider implements StorageProvider {
    private static String dbPath;
    private static final String DB_FILE = "users.sqlite";
    private static final String JDBC_URL = "jdbc:sqlite:";
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteStorageProvider.class);
    private static final Gson GSON = new Gson();

    public SQLiteStorageProvider(String stFolder) {
        dbPath = JDBC_URL + stFolder + FileSystems.getDefault().getSeparator() + DB_FILE;
        LOGGER.info("Storage path: {}", dbPath);
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(dbPath);
             Statement stm = connection.createStatement()) {

            String createTableQuery = "CREATE TABLE IF NOT EXISTS users (id BIGINT PRIMARY KEY, json TEXT)";
            stm.executeUpdate(createTableQuery);
            LOGGER.info("Initialize database {}", dbPath);

        } catch (SQLException e) {
            LOGGER.error("Database {} not found, creating new one.  Method initializeDatabase()", dbPath);
        }
    }

    @Override
    public User load(Long userId) {
        String selectQuery = "SELECT json FROM users WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(dbPath);
             PreparedStatement preparedStm = connection.prepareStatement(selectQuery)) {

            preparedStm.setLong(1, userId);
            ResultSet resultSet = preparedStm.executeQuery();

            if (resultSet.next()) {
                String json = resultSet.getString("json");
                LOGGER.info("<<< User {} loaded from database.", userId);
                return GSON.fromJson(json, User.class);
            }

        } catch (SQLException e) {
            LOGGER.error("Cant connect to database {} Method load(Long userId): {}", dbPath, userId, e);
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
                try (Connection connection = DriverManager.getConnection(dbPath)) {

                    Statement stm = connection.createStatement();
                    ResultSet resultSet = stm.executeQuery(selectAllQuery);

                    while (resultSet.next()) {
                        long userId = resultSet.getLong("id");
                        if (userId > 0) {
                            String userData = resultSet.getString("json");
                            User user = GSON.fromJson(userData, User.class);

                            LOGGER.info("<<< User {} loaded from database Thread: {}", user.getId(), Thread.currentThread().getName());
                            AppRegistry.addUserCompletely(user);
                        }
                    }

                } catch (SQLException e) {
                    LOGGER.error("Cant connect to database {} Method load()", dbPath, e);
                }
            }
        };
        new Thread(taskLoadUsers).start();
    }

    @Override
    public void save(User user) {
        if (!AppRegistry.getConfIsUsingUsersStorage() || user == null) {
            return;
        }
        Runnable taskSave = new Runnable() {
            public void run() {
                try (Connection connection = DriverManager.getConnection(dbPath)) {

                    PreparedStatement insertStm = connection.prepareStatement("INSERT INTO users (id, json) VALUES (?, ?)");
                    PreparedStatement updateStm = connection.prepareStatement("UPDATE users SET json = ? WHERE id = ?");

                    updateStm.setString(1, GSON.toJson(user));
                    updateStm.setLong(2, user.getId());
                    if (updateStm.executeUpdate() == 1) {
                        LOGGER.info(">>> User {} updated in database. Thread: {}", user.getId(), Thread.currentThread().getName());
                    } else {
                        insertStm.setLong(1, user.getId());
                        insertStm.setString(2, GSON.toJson(user));
                        insertStm.executeUpdate();
                        LOGGER.info(">>> User {} saved to database Thread: {}", user.getId(), Thread.currentThread().getName());
                    }

                } catch (SQLException e) {
                    LOGGER.error("Cant connect to database {} Method save(User user): {}", dbPath, user.getId(), e);
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
                try (Connection connection = DriverManager.getConnection(dbPath)) {

                    PreparedStatement deleteStm = connection.prepareStatement("DELETE FROM users WHERE id = ?");
                    deleteStm.setLong(1, userId);
                    deleteStm.executeUpdate();
                    LOGGER.info("XXX User {} deleted from database Thread: {}", userId, Thread.currentThread().getName());

                } catch (SQLException e) {
                    LOGGER.error("Cant connect to database {} Method delete(Long userId): {}", dbPath, userId, e);
                }
            }
        };
        new Thread(taskSave).start();
    }
}