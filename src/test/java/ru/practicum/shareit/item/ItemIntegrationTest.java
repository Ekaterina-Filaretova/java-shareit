package ru.practicum.shareit.item;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/schema.sql")
@Sql(scripts = "/delete-data.sql", executionPhase = AFTER_TEST_METHOD)
public class ItemIntegrationTest {

    private final String url = "/items";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ItemRepository repository;

    @BeforeEach
    public void setUp() throws Exception {
        UserDto user = new UserDto(null, "user", "mail@qwerty.com");
        UserDto user2 = new UserDto(null, "user2", "qwe@qwerty.com");
        ItemRequestDto request = new ItemRequestDto(null, "request", null, null);
        ItemDto item = new ItemDto(null, "item", "desc", true, null);
        ItemDto item2 = new ItemDto(null, "new item", "new desc", true, 1L);
        BookingDto bookingDto = new BookingDto(null, getLTD(1), getLTD(2),
                null, 1L, null, null, null);
        mockMvc.perform(postRequest(user));
        mockMvc.perform(postRequest(user2));
        mockMvc.perform(postRequest(request, 2L));
        mockMvc.perform(postRequest(item, 1L));
        mockMvc.perform(postRequest(item2, 2L));
        mockMvc.perform(postRequest(bookingDto, 2L));
    }

    @Test
    public void create() throws Exception {
        ItemDto item = new ItemDto(null, "item2", "desc2", true, null);
        mockMvc.perform(postRequest(item, 1L));

        Optional<Item> foundItem = repository.findById(3L);
        assertThat(foundItem.get().getId()).isEqualTo(3L);
        assertThat(foundItem.get().getName()).isEqualTo(item.getName());
        assertThat(foundItem.get().getDescription()).isEqualTo(item.getDescription());
        assertThat(foundItem.get().getAvailable()).isEqualTo(item.getAvailable());
        assertThat(foundItem.get().getOwner().getId()).isEqualTo(1L);
        assertThat(foundItem.get().getRequest()).isNull();
    }

    @Test
    public void update() throws Exception {
        ItemDto item = new ItemDto(null, "update", "update desc", false, null);
        mockMvc.perform(patchRequest(item, 1L, 1L));

        Optional<Item> foundItem = repository.findById(1L);
        assertThat(foundItem).isNotEmpty();
        assertThat(foundItem.get().getId()).isEqualTo(1L);
        assertThat(foundItem.get().getName()).isEqualTo(item.getName());
        assertThat(foundItem.get().getDescription()).isEqualTo(item.getDescription());
        assertThat(foundItem.get().getAvailable()).isEqualTo(item.getAvailable());
    }

    @Test
    public void getById() {
        Optional<Item> foundItem = repository.findById(1L);
        assertThat(foundItem.get().getId()).isEqualTo(1L);
        assertThat(foundItem.get().getName()).isEqualTo("item");
        assertThat(foundItem.get().getDescription()).isEqualTo("desc");
    }

    @Test
    public void getByOwner() {
        List<Item> items = repository.findByOwnerId(1L, Pageable.unpaged());
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getId()).isEqualTo(1L);
        assertThat(items.get(0).getName()).isEqualTo("item");
    }

    @Test
    public void searchByText() {
        List<Item> items = repository.findByText("ne", Pageable.unpaged());
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getId()).isEqualTo(2L);
        assertThat(items.get(0).getName()).isEqualTo("new item");
    }

    @Test
    public void addComment() throws Exception {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        CommentDto commentDto = new CommentDto(null, "text", null, null);
        mockMvc.perform(postRequest(commentDto, 1L, 2L));

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("item")))
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].text", is(commentDto.getText())));
    }

    @Test
    public void getAllByRequestId() {
        List<Item> items = repository.getAllByRequestId(1L);
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getId()).isEqualTo(2L);
        assertThat(items.get(0).getName()).isEqualTo("new item");
    }

    private MockHttpServletRequestBuilder postRequest(UserDto user) throws JsonProcessingException {
        return MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(user));
    }

    private MockHttpServletRequestBuilder postRequest(ItemDto item, Long ownerId) throws JsonProcessingException {
        return MockMvcRequestBuilders.post(url)
                .header("X-Sharer-User-Id", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(item));
    }

    private MockHttpServletRequestBuilder patchRequest(ItemDto item,
                                                       Long itemId,
                                                       Long ownerId) throws JsonProcessingException {
        return MockMvcRequestBuilders.patch(url + "/" + itemId)
                .header("X-Sharer-User-Id", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(item));
    }

    private MockHttpServletRequestBuilder postRequest(CommentDto commentDto,
                                                      Long itemId,
                                                      Long userId) throws JsonProcessingException {
        return MockMvcRequestBuilders.post(url + "/" + itemId + "/comment")
                .header("X-Sharer-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(commentDto));
    }

    private MockHttpServletRequestBuilder postRequest(BookingDto bookingDto,
                                                      Long userId) throws JsonProcessingException {
        return MockMvcRequestBuilders.post("/bookings")
                .header("X-Sharer-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(bookingDto));
    }

    private MockHttpServletRequestBuilder postRequest(ItemRequestDto itemRequestDto,
                                                      Long userId) throws JsonProcessingException {
        return MockMvcRequestBuilders.post("/requests")
                .header("X-Sharer-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(itemRequestDto));
    }

    private LocalDateTime getLTD(int sec) {
        return LocalDateTime.now().plusSeconds(sec);
    }
}
