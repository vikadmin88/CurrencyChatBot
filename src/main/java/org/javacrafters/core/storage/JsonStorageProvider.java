package org.javacrafters.core.storage;

import com.google.gson.Gson;
import org.javacrafters.core.AppRegistry;
import org.javacrafters.scheduler.Scheduler;
import org.javacrafters.user.User;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class JsonStorageProvider implements StorageProvider {

    private static final Gson GSON = new Gson();
    @Override
    public User load(Long userId) {
        String filePath = String.format("./botusers/user-%d-bot.json", userId);
        try (Reader reader = new FileReader(filePath)) {
            System.out.println("Loading user " + filePath);
            return GSON.fromJson(reader, User.class);
        } catch (IOException e) {
            System.out.println("File "+filePath+" not found.");
//            e.printStackTrace();
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
                try (Stream<Path> paths = Files.walk(Paths.get("./botusers"))) {

                    paths.filter((file) -> file.toString().endsWith("-bot.json")).forEach(file -> {

                        try (Reader reader = new FileReader(file.toString())) {
                            System.out.println("Loading users " + file);

                            User user = GSON.fromJson(reader, User.class);
                            AppRegistry.addUser(user);
                            Scheduler.addUserSchedule(user.getId(), user, user.getNotifyTime());
                            System.out.printf("User %d loaded from file: ./botusers/user-%d-bot.json (thread: %s)\n",user.getId(), user.getId(), Thread.currentThread().getName());

                        } catch (IOException e) {
//                            e.printStackTrace();
                        }

                    });

                } catch (IOException e) {
//                    throw new RuntimeException(e);
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
                    Files.createDirectories(Paths.get("./botusers"));
                    try (FileWriter writer = new FileWriter("./botusers/user-" + user.getId() + "-bot.json")) {
                        GSON.toJson(user, writer);
                        System.out.printf("User %d saved to file: ./botusers/user-%d-bot.json (thread: %s)\n",user.getId(), user.getId(), Thread.currentThread().getName());
                    }
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }
        };
        new Thread(taskSave).start();
    }

}
