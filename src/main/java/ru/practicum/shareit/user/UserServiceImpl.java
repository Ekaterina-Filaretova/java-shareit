package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper userMapper;

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
        log.info("Добавлен пользователь {}", user);
        return userMapper.toDto(repository.save(user));
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long userId) {
        User user = userMapper.fromDto(userDto);
        user.setId(userId);
        Optional<User> updatedUser = repository.findById(user.getId());
        if (updatedUser.isEmpty()) {
            throw new ObjectNotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        if (user.getName() != null) {
            updatedUser.get().setName(user.getName());
        }
        if (user.getEmail() != null) {
            updatedUser.get().setEmail(user.getEmail());
        }
        log.info("Обновлен пользователь {}", updatedUser);
        return userMapper.toDto(repository.save(updatedUser.get()));
    }

    @Override
    public UserDto getUserById(Long userId) {
        Optional<User> user = repository.findById(userId);
        if (user.isEmpty()) {
            throw new ObjectNotFoundException("Пользователь с id " + userId + " не найден");
        }
        log.info("Получен пользователь {}", user.get());
        return userMapper.toDto(user.get());
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = repository.findAll();
        log.info("Получен список пользователей {}", users);
        List<UserDto> userDtos = new ArrayList<>();
        for (User user : users) {
            userDtos.add(userMapper.toDto(user));
        }
        return userDtos;
    }

    @Override
    public void removeUser(Long userId) {
        Optional<User> user = repository.findById(userId);
        if (user.isEmpty()) {
            throw new ObjectNotFoundException("Пользователь с id " + userId + " не найден");
        }
        log.info("Удален пользователь {}", user.get());
        repository.deleteById(userId);
    }
}