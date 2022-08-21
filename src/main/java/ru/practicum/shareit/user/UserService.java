package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {

    User addUser(User user);

    User updateUser(User user);

    User getUserById(Long userId);

    List<User> getAllUsers();

    void removeUser(Long userId);
}
