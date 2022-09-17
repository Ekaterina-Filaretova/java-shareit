package ru.practicum.shareit.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/schema.sql")
@Sql(scripts = "/delete-data.sql", executionPhase = AFTER_TEST_METHOD)
public class ItemRequestIntegrationTest {

    private final String url = "/requests";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ItemRequestRepository repository;

    @BeforeEach
    public void setUp() throws Exception {
        UserDto requester = new UserDto(null, "requester", "qwe@mail.com");
        UserDto requester2 = new UserDto(null, "requester2", "asd@mail.com");
        ItemRequestDto request = new ItemRequestDto(null, "text", null, null);
        ItemRequestDto request2 = new ItemRequestDto(null, "text2", null, null);
        ItemRequestDto request3 = new ItemRequestDto(null, "text3", null, null);
        mockMvc.perform(postRequest(requester));
        mockMvc.perform(postRequest(requester2));
        mockMvc.perform(postRequest(request, 1L));
        mockMvc.perform(postRequest(request2, 2L));
        mockMvc.perform(postRequest(request3, 2L));
    }

    @Test
    public void addRequest() throws Exception {
        ItemRequestDto request = new ItemRequestDto(null, "text4", null, null);
        mockMvc.perform(postRequest(request, 2L));

        Optional<ItemRequest> foundRequest = repository.findById(4L);
        assertThat(foundRequest).isNotEmpty();
        assertThat(foundRequest.get().getDescription()).isEqualTo(request.getDescription());
        assertThat(foundRequest.get().getRequester().getId()).isEqualTo(2L);
        assertThat(foundRequest.get().getCreated()).isNotNull();
    }

    @Test
    public void getAllByRequester() {
        List<ItemRequest> requests = repository.findAllByRequesterId(2L, Sort.unsorted());

        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getId()).isEqualTo(2L);
        assertThat(requests.get(0).getDescription()).isEqualTo("text2");
        assertThat(requests.get(1).getId()).isEqualTo(3L);
        assertThat(requests.get(1).getDescription()).isEqualTo("text3");
    }

    @Test
    public void getAllExceptRequester() {
        List<ItemRequest> requests = repository.findAllByRequesterIdNot(1L, Pageable.unpaged());

        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getId()).isEqualTo(2L);
        assertThat(requests.get(0).getDescription()).isEqualTo("text2");
        assertThat(requests.get(1).getId()).isEqualTo(3L);
        assertThat(requests.get(1).getDescription()).isEqualTo("text3");
    }

    @Test
    public void getById() {
        Optional<ItemRequest> request = repository.findById(3L);

        assertThat(request).isNotEmpty();
        assertThat(request.get().getRequester().getId()).isEqualTo(2L);
        assertThat(request.get().getDescription()).isEqualTo("text3");
    }

    private MockHttpServletRequestBuilder postRequest(UserDto user) throws JsonProcessingException {
        return MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(user));
    }

    private MockHttpServletRequestBuilder postRequest(ItemRequestDto itemRequestDto,
                                                      Long requesterId) throws JsonProcessingException {
        return MockMvcRequestBuilders.post(url)
                .header("X-Sharer-User-Id", requesterId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(itemRequestDto));
    }
}
