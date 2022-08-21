package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectAlreadyExistException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private Long id = 1L;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User addUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || user.getEmail().isEmpty()) {
            throw new ValidationException("Почта не должна быть пустой");
        }
        checkEmail(user.getEmail());
        user.setId(id++);
        log.info("Добавлен пользователь {}", user);
        return repository.addUser(user);
    }

    @Override
    public User updateUser(User user) {
        User updatedUser = getUserById(user.getId());
        if (updatedUser != null) {
            if (user.getName() != null) {
                updatedUser.setName(user.getName());
            }
            if (user.getEmail() != null) {
                checkEmail(user.getEmail());
                updatedUser.setEmail(user.getEmail());
            }
            log.info("Обновлен пользователь {}", updatedUser);
            return repository.updateUser(updatedUser);
        }
        throw new ObjectNotFoundException("Пользователь с id " + user.getId() + " не найден");
    }

    @Override
    public User getUserById(Long userId) {
        User user = repository.getUserById(userId);
        log.info("Получен пользователь {}", user);
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = repository.getAllUsers();
        log.info("Получен список пользователей {}", users);
        return users;
    }

    @Override
    public void removeUser(Long userId) {
        User user = repository.getUserById(userId);
        if (user != null) {
            log.info("Удален пользователь {}", user);
            repository.removeUser(userId);
            return;
        }
        throw new ObjectNotFoundException("Пользователь с id " + userId + " не найден");
    }

    private void checkEmail(String email) {
        for (User user : getAllUsers()) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                throw new ObjectAlreadyExistException("Пользователь с почтой " + email + " уже есть");
            }
        }
    }
}
