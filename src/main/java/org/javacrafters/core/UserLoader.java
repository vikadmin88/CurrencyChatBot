package org.javacrafters.core;

import org.javacrafters.core.storage.StorageProvider;
import org.javacrafters.user.User;

public class UserLoader {

    private static StorageProvider storageProvider;

    private UserLoader() {
    }

    public static StorageProvider setStorageProvider(StorageProvider provider) {
        storageProvider = provider;
        return storageProvider;
    }

    public static User load(Long userId) {
        return storageProvider.load(userId);
    }

    public static void load() {
        storageProvider.load();
    }

    public static void save(User user) {
        storageProvider.save(user);
    }

}