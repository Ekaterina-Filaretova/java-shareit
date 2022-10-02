package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserMapper mapper;

    @Mock
    private UserRepository repository;

    @Test
    public void addUser() {
        UserDto userDto = new UserDto(1L, "user", "qwe@mail.com");
        User user = new User(1L, "user", "qwe@mail.com");
        when(mapper.fromDto(userDto)).thenReturn(user);
        when((mapper.toDto(user))).thenReturn(userDto);
        when(repository.save(any())).thenReturn(user);

        UserDto savedUser = userService.addUser(userDto);
        assertThat(savedUser).usingRecursiveComparison().isEqualTo(userDto);
    }

    @Test
    public void addUserWithoutMail() {
        User userDto = new User(1L, "user", null);
        when(mapper.fromDto(any())).thenReturn(userDto);

        assertThatThrownBy(() -> userService.addUser(new UserDto()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void addUserWithEmptyMail() {
        User userDto = new User(1L, "user", "");
        when(mapper.fromDto(any())).thenReturn(userDto);

        assertThatThrownBy(() -> userService.addUser(new UserDto()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void addUserWithBlankMail() {
        User userDto = new User(1L, "user", "   ");
        when(mapper.fromDto(any())).thenReturn(userDto);

        assertThatThrownBy(() -> userService.addUser(new UserDto()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void updateUser() {
        UserDto userDto = new UserDto(1L, "new user", "asd@mail.com");
        User user = new User(1L, "user", "qwe@mail.com");
        when(repository.findById(anyLong())).thenReturn(Optional.of(user));
        when(mapper.fromDto(userDto)).thenReturn(user);
        when(repository.save(any())).thenReturn(user);
        when((mapper.toDto(user))).thenReturn(userDto);

        UserDto updatedUser = userService.updateUser(userDto, userDto.getId());
        assertThat(updatedUser).usingRecursiveComparison().isEqualTo(userDto);
    }

    @Test
    public void updateUserWithOnlyName() {
        UserDto userDto = new UserDto(1L, "new user", null);
        User user = new User(1L, "user", "qwe@mail.com");
        when(repository.findById(anyLong())).thenReturn(Optional.of(user));
        when(mapper.fromDto(userDto)).thenReturn(user);
        when(repository.save(any())).thenReturn(user);
        when((mapper.toDto(user))).thenReturn(new UserDto(1L, "new user", "qwe@mail.com"));

        UserDto updatedUser = userService.updateUser(userDto, userDto.getId());
        assertThat(userDto.getId()).isSameAs(updatedUser.getId());
        assertThat(userDto.getName()).isSameAs(updatedUser.getName());
        assertThat(user.getEmail()).isSameAs(updatedUser.getEmail());
    }

    @Test
    public void updateUserWithOnlyMail() {
        UserDto userDto = new UserDto(1L, null, "asd@mail.com");
        User user = new User(1L, "user", "qwe@mail.com");
        when(repository.findById(anyLong())).thenReturn(Optional.of(user));
        when(mapper.fromDto(userDto)).thenReturn(user);
        when(repository.save(any())).thenReturn(user);
        when((mapper.toDto(user))).thenReturn(new UserDto(1L, "user", "asd@mail.com"));

        UserDto updatedUser = userService.updateUser(userDto, userDto.getId());
        assertThat(userDto.getId()).isSameAs(updatedUser.getId());
        assertThat(user.getName()).isSameAs(updatedUser.getName());
        assertThat(userDto.getEmail()).isSameAs(updatedUser.getEmail());
    }

    @Test
    public void updateUserWithWrongId() {
        UserDto userDto = new UserDto(1L, "user", "qwe@mail.com");
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.updateUser(userDto, userDto.getId()))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getById() {
        UserDto userDto = new UserDto(1L, "user", "qwe@mail.com");
        User user = new User(1L, "user", "qwe@mail.com");
        when((mapper.toDto(user))).thenReturn(userDto);
        when(repository.findById(anyLong())).thenReturn(Optional.of(user));

        UserDto foundUser = userService.getUserById(userDto.getId());
        assertThat(foundUser).usingRecursiveComparison().isEqualTo(userDto);
    }

    @Test
    public void getByWrongId() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.getUserById(1L))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getAll() {
        UserDto userDto = new UserDto(1L, "user", "qwe@mail.com");
        UserDto userDto2 = new UserDto(2L, "user2", "asd@mail.com");
        User user = new User(1L, "user", "qwe@mail.com");
        User user2 = new User(2L, "user2", "asd@mail.com");
        when((mapper.toDto(user))).thenReturn(userDto);
        when((mapper.toDto(user2))).thenReturn(userDto2);
        when(repository.findAll()).thenReturn(List.of(user, user2));

        List<UserDto> users = userService.getAllUsers();
        assertThat(users.size()).isEqualTo(2);
        assertThat(users.get(0)).usingRecursiveComparison().isEqualTo(userDto);
        assertThat(users.get(1)).usingRecursiveComparison().isEqualTo(userDto2);
    }

    @Test
    public void delete() {
        when(repository.findById(any())).thenReturn(Optional.of(new User()));
        userService.removeUser(1L);

        verify(repository, times(1)).deleteById(any());
    }

    @Test
    public void deleteByWrongId() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.removeUser(1L))
                .isInstanceOf(ObjectNotFoundException.class);
    }
}
