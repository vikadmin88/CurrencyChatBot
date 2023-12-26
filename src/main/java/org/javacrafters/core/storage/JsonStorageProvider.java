package org.javacrafters.core.storage;

import com.google.gson.Gson;
import org.javacrafters.core.AppRegistry;
import org.javacrafters.scheduler.Scheduler;
import org.javacrafters.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class JsonStorageProvider implements StorageProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonStorageProvider.class);
    private static String storageFolder;
    private static final Gson GSON = new Gson();

    public JsonStorageProvider(String stFolder) {
        storageFolder = stFolder;
        LOGGER.info("Storage folder: {}", stFolder);
    }

    @Override
    public User load(Long userId) {
        String filePath = String.format(storageFolder + FileSystems.getDefault().getSeparator() + "user-%d-bot.json", userId);
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
                try (Stream<Path> paths = Files.walk(Paths.get(storageFolder))) {

                    paths.filter((file) -> file.getFileName().toString().matches("user-([0-9]+)+-bot.json")).forEach(file -> {

                        try (Reader reader = new FileReader(file.toString())) {
                            LOGGER.info("Loading user {}", file);
                            User user = GSON.fromJson(reader, User.class);
                            if (user.getId() > 0) {
                                AppRegistry.addUser(user);
                                LOGGER.info("User {} loaded from file: {}/user-{}-bot.json (thread: {})", user.getId(), storageFolder, user.getId(), Thread.currentThread().getName());
                                if (user.isNotifyOn()) {
                                    Scheduler.addUserSchedule(user.getId(), user, user.getNotifyTime());
                                }
                            }
                        } catch (IOException e) {
                            LOGGER.error("Not Loading users", e);
                        }

                    });

                } catch (IOException e) {
                    LOGGER.error("Folder {} not found!", storageFolder, e);
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
                try {
                    Files.createDirectories(Paths.get(storageFolder));
                    try (FileWriter writer = new FileWriter(storageFolder + FileSystems.getDefault().getSeparator() + "user-" + user.getId() + "-bot.json")) {
                        GSON.toJson(user, writer);
                        LOGGER.info("User {} saved to file: {}/user-{}-bot.json (thread: {})", user.getId(), storageFolder, user.getId(), Thread.currentThread().getName());
                    }
                } catch (IOException e) {
                    LOGGER.error("Can't create {}", storageFolder, e);
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
                File file = new File(storageFolder + FileSystems.getDefault().getSeparator() + "user-" + userId + "-bot.json");
                if (file.delete()) {
                    LOGGER.info("Deleted file {}/{}", storageFolder, file.getName());
                } else {
                    LOGGER.error("Can't delete {}/{}", storageFolder, file.getName());
                }
            }
        };
        new Thread(taskDeleteUser).start();
    }
}