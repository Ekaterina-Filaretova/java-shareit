package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectAlreadyExistException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper userMapper;
    private Long id = 1L;

    public UserServiceImpl(UserRepository repository, UserMapper userMapper) {
        this.repository = repository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        User user = userMapper.fromDto(userDto);
        if (user.getEmail() == null || user.getEmail().isBlank() || user.getEmail().isEmpty()) {
            throw new ValidationException("Почта не должна быть пустой");
        }
        checkEmail(user.getEmail());
        user.setId(id++);
        log.info("Добавлен пользователь {}", user);
        return userMapper.toDto(repository.addUser(user));
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long userId) {
        User user = userMapper.fromDto(userDto);
        user.setId(userId);
        User updatedUser = repository.getUserById(user.getId());
        if (updatedUser != null) {
            if (user.getName() != null) {
                updatedUser.setName(user.getName());
            }
            if (user.getEmail() != null) {
                checkEmail(user.getEmail());
                updatedUser.setEmail(user.getEmail());
            }
            log.info("Обновлен пользователь {}", updatedUser);
            return userMapper.toDto(repository.updateUser(updatedUser));
        }
        throw new ObjectNotFoundException("Пользователь с id " + user.getId() + " не найден");
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = repository.getUserById(userId);
        if (user != null) {
            log.info("Получен пользователь {}", user);
            return userMapper.toDto(repository.getUserById(userId));
        }
        throw new ObjectNotFoundException("Пользователь с id " + userId + " не найден");
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = repository.getAllUsers();
        log.info("Получен список пользователей {}", users);
        List<UserDto> userDtos = new ArrayList<>();
        for (User user : users) {
            userDtos.add(userMapper.toDto(user));
        }
        return userDtos;
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
        for (User user : repository.getAllUsers()) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                throw new ObjectAlreadyExistException("Пользователь с почтой " + email + " уже есть");
            }
        }
    }
}
