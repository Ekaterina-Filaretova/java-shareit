package ru.practicum.shareit.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/schema.sql")
@Sql(scripts = "/delete-data.sql", executionPhase = AFTER_TEST_METHOD)
public class UserIntegrationTest {

    private final String url = "/users";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository repository;

    @BeforeEach
    public void setUp() throws Exception {
        UserDto user = new UserDto(null, "name", "qwe@qwerty.com");
        UserDto user2 = new UserDto(null, "name2", "asd@qwerty.com");
        mockMvc.perform(postRequest(user));
        mockMvc.perform(postRequest(user2));
    }

    @Test
    public void create() throws Exception {
        UserDto user = new UserDto(null, "name", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        Optional<User> foundUser = repository.findById(3L);
        assertThat(foundUser.get().getId()).isEqualTo(3L);
        assertThat(foundUser.get().getName()).isEqualTo(user.getName());
        assertThat(foundUser.get().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    public void update() throws Exception {
        UserDto user = new UserDto(2L, "qwerty", "update@qwerty.com");
        mockMvc.perform(patchRequest(user, user.getId()));

        Optional<User> foundUser = repository.findById(2L);
        assertThat(foundUser.get().getId()).isEqualTo(2L);
        assertThat(foundUser.get().getName()).isEqualTo(user.getName());
        assertThat(foundUser.get().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    public void getById() {
        Optional<User> foundUser = repository.findById(1L);
        assertThat(foundUser.get().getId()).isEqualTo(1L);
        assertThat(foundUser.get().getName()).isEqualTo("name");
        assertThat(foundUser.get().getEmail()).isEqualTo("qwe@qwerty.com");
    }

    @Test
    public void getAll() {
        List<User> users = repository.findAll();
        assertThat(users).hasSize(2);
        assertThat(users.get(0).getId()).isEqualTo(1L);
        assertThat(users.get(0).getName()).isEqualTo("name");
        assertThat(users.get(1).getId()).isEqualTo(2L);
        assertThat(users.get(1).getName()).isEqualTo("name2");
    }

    @Test
    public void delete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(url + "/1"));

        Optional<User> foundUser = repository.findById(1L);
        assertThat(foundUser).isEmpty();
    }

    private @NotNull MockHttpServletRequestBuilder postRequest(UserDto user) throws JsonProcessingException {
        return MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(user));
    }

    private @NotNull MockHttpServletRequestBuilder patchRequest(UserDto user,
                                                                Long userId) throws JsonProcessingException {
        return MockMvcRequestBuilders.patch(url + "/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(user));
    }
}
