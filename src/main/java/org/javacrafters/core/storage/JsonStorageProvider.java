package org.javacrafters.core.storage;

import com.google.gson.Gson;
import org.javacrafters.core.AppRegistry;
import org.javacrafters.scheduler.Scheduler;
import org.javacrafters.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class JsonStorageProvider implements StorageProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonStorageProvider.class);
    private static final String STORAGE_FOLDER = "./botusers";
    private static final Gson GSON = new Gson();

    public JsonStorageProvider() {
    }

    @Override
    public User load(Long userId) {
        String filePath = String.format(STORAGE_FOLDER + "/user-%d-bot.json", userId);
        try (Reader reader = new FileReader(filePath)) {
            LOGGER.info("Loading user {}", userId);
            return GSON.fromJson(reader, User.class);
        } catch (IOException e) {
            LOGGER.error("File {} not found.", filePath);
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
                try (Stream<Path> paths = Files.walk(Paths.get(STORAGE_FOLDER))) {

                    paths.filter((file) -> file.toString().endsWith("-bot.json")).forEach(file -> {

                        try (Reader reader = new FileReader(file.toString())) {
                            LOGGER.info("Loading user {}", file);
                            User user = GSON.fromJson(reader, User.class);
                            AppRegistry.addUser(user);
                            Scheduler.addUserSchedule(user.getId(), user, user.getNotifyTime());
                            LOGGER.info("User {} loaded from file: {}/user-{}-bot.json (thread: {})", user.getId(), user.getId(), STORAGE_FOLDER, Thread.currentThread().getName());
                        } catch (IOException e) {
                            LOGGER.error("Not Loading users", e);
                        }

                    });

                } catch (IOException e) {
                    LOGGER.error("Folder {} not found!", STORAGE_FOLDER, e);
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
                try {
                    Files.createDirectories(Paths.get(STORAGE_FOLDER));
                    try (FileWriter writer = new FileWriter(STORAGE_FOLDER + "/user-" + user.getId() + "-bot.json")) {
                        GSON.toJson(user, writer);
                        LOGGER.info("User {} saved to file: {}/user-{}-bot.json (thread: {})", user.getId(), STORAGE_FOLDER, user.getId(), Thread.currentThread().getName());
                    }
                } catch (IOException e) {
                    LOGGER.error("Can't create {}", STORAGE_FOLDER, e);
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
        Runnable taskDeleteUser = new Runnable() {
            public void run() {
                File file = new File(STORAGE_FOLDER + "/user-" + userId + "-bot.json");
                if (file.delete()) {
                    LOGGER.error("Deleted file {}/{}", STORAGE_FOLDER, file.getName());
                } else {
                    LOGGER.error("Can't delete {}/{}", STORAGE_FOLDER, file.getName());
                }
            }
        };
        new Thread(taskDeleteUser).start();
    }
}