package ru.practicum.shareit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.NotNull;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerTest {

    private final String url = "/users";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void create() throws Exception {
        UserDto user = new UserDto(null, "name", "mail@qwerty.com");

        mockMvc.perform(postRequest(user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("name")));
    }

    @Test
    public void createWithDuplicateMail() throws Exception {
        UserDto user = new UserDto(null, "name", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        UserDto user2 = new UserDto(null, "qwerty", "mail@qwerty.com");
        mockMvc.perform(postRequest(user2))
                .andExpect(status().isConflict());
    }

    @Test
    public void createWithoutMail() throws Exception {
        UserDto user = new UserDto(null, "name", null);
        mockMvc.perform(postRequest(user))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createWithInvalidMail() throws Exception {
        UserDto user = new UserDto(null, "name", "qwerty.com");
        mockMvc.perform(postRequest(user))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void update() throws Exception {
        UserDto user = new UserDto(null, "name", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        UserDto user2 = new UserDto(1L, "qwerty", "update@qwerty.com");

        mockMvc.perform(patchRequest(user2, user2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("qwerty")))
                .andExpect(jsonPath("$.email", is("update@qwerty.com")));
    }

    @Test
    public void updateOnlyName() throws Exception {
        UserDto user = new UserDto(null, "name", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        UserDto user2 = new UserDto(1L, "qwerty", null);

        mockMvc.perform(patchRequest(user2, user2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("qwerty")))
                .andExpect(jsonPath("$.email", is("mail@qwerty.com")));
    }

    @Test
    public void updateOnlyMail() throws Exception {
        UserDto user = new UserDto(null, "name", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        UserDto user2 = new UserDto(1L, null, "update@qwerty.com");

        mockMvc.perform(patchRequest(user2, user2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("name")))
                .andExpect(jsonPath("$.email", is("update@qwerty.com")));
    }

    @Test
    public void updateWithMailExist() throws Exception {
        UserDto user = new UserDto(null, "name", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        UserDto user2 = new UserDto(1L, "qwerty", "mail@qwerty.com");

        mockMvc.perform(patchRequest(user2, user2.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    public void delete() throws Exception {
        UserDto user = new UserDto(null, "name", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        mockMvc.perform(MockMvcRequestBuilders.delete(url + "/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void getById() throws Exception {
        UserDto user = new UserDto(null, "name", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("name")));
    }

    @Test
    public void getAll() throws Exception {
        UserDto user = new UserDto(null, "name", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        UserDto user2 = new UserDto(null, "qwerty", "qwerty@qwerty.com");
        mockMvc.perform(postRequest(user2));

        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("name")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("qwerty")));
    }

    private @NotNull MockHttpServletRequestBuilder postRequest(UserDto user) throws JsonProcessingException {
        return MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(user));
    }

    private @NotNull MockHttpServletRequestBuilder patchRequest(UserDto user, Long userId)
            throws JsonProcessingException {
        return MockMvcRequestBuilders.patch(url + "/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(user));
    }
}
