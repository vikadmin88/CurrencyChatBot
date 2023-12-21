package org.javacrafters.core.storage;

import org.javacrafters.user.User;

public interface StorageProvider {

    void save(User user);
    void load();
    User load(Long userId);
}
