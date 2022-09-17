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
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.NotNull;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemControllerTest {

    private final String url = "/items";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void create() throws Exception {
        UserDto user = new UserDto(null, "user", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        ItemDto item = new ItemDto(null, "name", "desc", true, null);
        mockMvc.perform(postRequest(item, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("name")));
    }

    @Test
    public void createWithoutHeader() throws Exception {
        UserDto user = new UserDto(null, "user", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        ItemDto item = new ItemDto(null, "name", "desc", true, null);
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createWithoutOwner() throws Exception {
        ItemDto item = new ItemDto(null, "name", "desc", true, null);
        mockMvc.perform(postRequest(item, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createWithoutAvailable() throws Exception {
        UserDto user = new UserDto(null, "user", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        ItemDto item = new ItemDto(null, "name", "desc", null, null);
        mockMvc.perform(postRequest(item, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createWithoutName() throws Exception {
        UserDto user = new UserDto(null, "user", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        ItemDto item = new ItemDto(null, null, "desc", true, null);
        mockMvc.perform(postRequest(item, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createWithoutDescription() throws Exception {
        UserDto user = new UserDto(null, "user", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        ItemDto item = new ItemDto(null, "name", null, true, null);
        mockMvc.perform(postRequest(item, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void update() throws Exception {
        UserDto user = new UserDto(null, "user", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        ItemDto item = new ItemDto(null, "name", "desc", true, null);
        mockMvc.perform(postRequest(item, 1L));

        ItemDto item2 = new ItemDto(null, "update", "update desc", false, null);
        mockMvc.perform(patchRequest(item2, 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("update")))
                .andExpect(jsonPath("$.description", is("update desc")))
                .andExpect(jsonPath("$.available", is(false)));
    }

    @Test
    public void updateWithoutHeader() throws Exception {
        UserDto user = new UserDto(null, "user", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        ItemDto item = new ItemDto(null, "name", "desc", true, null);
        mockMvc.perform(postRequest(item, 1L));

        ItemDto item2 = new ItemDto(null, "update", "update desc", false, null);
        mockMvc.perform(MockMvcRequestBuilders.patch(url + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(item2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateWithWrongOwner() throws Exception {
        UserDto user = new UserDto(null, "user", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        ItemDto item = new ItemDto(null, "name", "desc", true, null);
        mockMvc.perform(postRequest(item, 1L));

        ItemDto item2 = new ItemDto(null, "update", "update desc", false, null);
        mockMvc.perform(patchRequest(item2, 1L, 2L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateOnlyAvailable() throws Exception {
        UserDto user = new UserDto(null, "user", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        ItemDto item = new ItemDto(null, "name", "desc", true, null);
        mockMvc.perform(postRequest(item, 1L));

        ItemDto item2 = new ItemDto(null, null, null, false, null);
        mockMvc.perform(patchRequest(item2, 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("name")))
                .andExpect(jsonPath("$.description", is("desc")))
                .andExpect(jsonPath("$.available", is(false)));
    }

    @Test
    public void updateOnlyName() throws Exception {
        UserDto user = new UserDto(null, "user", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        ItemDto item = new ItemDto(null, "name", "desc", true, null);
        mockMvc.perform(postRequest(item, 1L));

        ItemDto item2 = new ItemDto(null, "new name", null, true, null);
        mockMvc.perform(patchRequest(item2, 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("new name")))
                .andExpect(jsonPath("$.description", is("desc")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    public void updateOnlyDescription() throws Exception {
        UserDto user = new UserDto(null, "user", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        ItemDto item = new ItemDto(null, "name", "desc", true, null);
        mockMvc.perform(postRequest(item, 1L));

        ItemDto item2 = new ItemDto(null, null, "new desc", true, null);
        mockMvc.perform(patchRequest(item2, 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("name")))
                .andExpect(jsonPath("$.description", is("new desc")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    public void getById() throws Exception {
        UserDto user = new UserDto(null, "user", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        ItemDto item = new ItemDto(null, "name", "desc", true, null);
        mockMvc.perform(postRequest(item, 1L));

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/1")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("name")))
                .andExpect(jsonPath("$.description", is("desc")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    public void getByOwner() throws Exception {
        UserDto user = new UserDto(null, "user", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        UserDto user2 = new UserDto(null, "new user", "qwer@qwerty.com");
        mockMvc.perform(postRequest(user2));

        ItemDto item = new ItemDto(null, "name", "desc", true, null);
        mockMvc.perform(postRequest(item, 1L));

        ItemDto item2 = new ItemDto(null, "new name", "new desc", false, null);
        mockMvc.perform(postRequest(item2, 1L));

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("name")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("new name")));

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void searchByName() throws Exception {
        UserDto user = new UserDto(null, "user", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        ItemDto item = new ItemDto(null, "name", "desc", true, null);
        mockMvc.perform(postRequest(item, 1L));

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/search?text=aMe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("name")));
    }

    @Test
    public void searchByDescription() throws Exception {
        UserDto user = new UserDto(null, "user", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        ItemDto item = new ItemDto(null, "name", "desc", true, null);
        mockMvc.perform(postRequest(item, 1L));

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/search?text=dEs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("name")));
    }

    @Test
    public void searchNotAvailable() throws Exception {
        UserDto user = new UserDto(null, "user", "mail@qwerty.com");
        mockMvc.perform(postRequest(user));

        ItemDto item = new ItemDto(null, "name", "desc", false, null);
        mockMvc.perform(postRequest(item, 1L));

        ItemDto item2 = new ItemDto(null, "new name", "new desc", true, null);
        mockMvc.perform(postRequest(item2, 1L));

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/search?text=dEs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].name", is("new name")));
    }

    private @NotNull MockHttpServletRequestBuilder postRequest(UserDto user) throws JsonProcessingException {
        return MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(user));
    }

    private @NotNull MockHttpServletRequestBuilder postRequest(ItemDto item,
                                                               Long ownerId) throws JsonProcessingException {
        return MockMvcRequestBuilders.post(url)
                .header("X-Sharer-User-Id", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(item));
    }

    private @NotNull MockHttpServletRequestBuilder patchRequest(ItemDto item,
                                                                Long itemId,
                                                                Long ownerId) throws JsonProcessingException {
        return MockMvcRequestBuilders.patch(url + "/" + itemId)
                .header("X-Sharer-User-Id", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(item));
    }
}
