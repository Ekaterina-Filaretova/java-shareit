package ru.practicum.shareit.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.exception.ObjectAlreadyExistException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    private final String url = "/users";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void addUser() throws Exception {
        UserDto user = new UserDto(1L, "user", "qwe@mail.com");
        when(userService.addUser(any())).thenReturn(user);

        mockMvc.perform(postRequest(user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));
    }

    @Test
    public void addWithMailExist() throws Exception {
        UserDto user = new UserDto(1L, "user", "qwe@mail.com");
        when(userService.addUser(any())).thenThrow(ObjectAlreadyExistException.class);

        mockMvc.perform(postRequest(user))
                .andExpect(status().isConflict());
    }

    @Test
    public void addWithoutMail() throws Exception {
        UserDto user = new UserDto(1L, "user", null);
        when(userService.addUser(any())).thenThrow(ValidationException.class);

        mockMvc.perform(postRequest(user))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addWithInvalidMail() throws Exception {
        UserDto user = new UserDto(1L, "user", "qwe");

        mockMvc.perform(postRequest(user))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void update() throws Exception {
        UserDto user = new UserDto(1L, "user", "qwe@mail.com");
        when(userService.updateUser(any(), any())).thenReturn(user);

        mockMvc.perform(patchRequest(user.getId(), user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));
    }

    @Test
    public void updateNameOnly() throws Exception {
        UserDto user = new UserDto(null, "user", null);
        UserDto updatedUser = new UserDto(1L, "user", "qwe@mail.com");
        when(userService.updateUser(any(), any())).thenReturn(updatedUser);

        mockMvc.perform(patchRequest(1L, user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedUser.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updatedUser.getName())))
                .andExpect(jsonPath("$.email", is(updatedUser.getEmail())));
    }

    @Test
    public void updateMailOnly() throws Exception {
        UserDto user = new UserDto(null, null, "qwe@mail.com");
        UserDto updatedUser = new UserDto(1L, "user", "qwe@mail.com");
        when(userService.updateUser(any(), any())).thenReturn(updatedUser);

        mockMvc.perform(patchRequest(1L, user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedUser.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updatedUser.getName())))
                .andExpect(jsonPath("$.email", is(updatedUser.getEmail())));
    }

    @Test
    public void updateWithMailExist() throws Exception {
        UserDto user = new UserDto(1L, "user", "qwe@mail.com");
        when(userService.updateUser(any(), any())).thenThrow(ObjectAlreadyExistException.class);

        mockMvc.perform(patchRequest(user.getId(), user))
                .andExpect(status().isConflict());
    }

    @Test
    public void updateWithInvalidMail() throws Exception {
        UserDto user = new UserDto(1L, "user", "qwe");

        mockMvc.perform(patchRequest(user.getId(), user))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void delete() throws Exception {
        doNothing().when(userService).removeUser(anyLong());

        mockMvc.perform(MockMvcRequestBuilders.delete(url + "/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void getById() throws Exception {
        UserDto user = new UserDto(1L, "user", "qwe@mail.com");
        when(userService.getUserById(any())).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));
    }

    @Test
    public void getAll() throws Exception {
        UserDto user = new UserDto(1L, "user", "qwe@mail.com");
        UserDto user2 = new UserDto(2L, "new user", "asd@mail.com");
        when(userService.getAllUsers()).thenReturn(List.of(user, user2));

        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(user.getName())))
                .andExpect(jsonPath("$[0].email", is(user.getEmail())))
                .andExpect(jsonPath("$[1].id", is(user2.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(user2.getName())))
                .andExpect(jsonPath("$[1].email", is(user2.getEmail())));
    }

    private MockHttpServletRequestBuilder postRequest(UserDto user) throws JsonProcessingException {
        return MockMvcRequestBuilders.post(url)
                .content(objectMapper.writeValueAsString(user))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder patchRequest(Long userId, UserDto user) throws JsonProcessingException {
        return MockMvcRequestBuilders.patch(url + "/" + userId)
                .content(objectMapper.writeValueAsString(user))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }
}
