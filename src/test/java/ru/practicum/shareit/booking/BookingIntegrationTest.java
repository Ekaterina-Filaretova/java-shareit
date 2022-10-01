package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/schema.sql")
@Sql(scripts = "/delete-data.sql", executionPhase = AFTER_TEST_METHOD)
public class BookingIntegrationTest {

    private final String url = "/bookings";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BookingRepository repository;

    @BeforeEach
    public void setUp() throws Exception {
        UserDto owner = new UserDto(null, "owner", "mail@qwerty.com");
        UserDto booker = new UserDto(null, "booker", "asd@qwerty.com");
        ItemDto item = new ItemDto(null, "item", "desc", true, null);
        ItemDto item2 = new ItemDto(null, "item2", "desc2", true, null);
        BookingDto bookingDto = new BookingDto(null, getLTD(2), getLTD(3), null, 1L,
                null, null, null);
        BookingDto bookingDto2 = new BookingDto(null, getLTD(2), getLTD(3), null, 2L,
                null, null, null);
        mockMvc.perform(postRequest(owner));
        mockMvc.perform(postRequest(booker));
        mockMvc.perform(postRequest(item, 1L));
        mockMvc.perform(postRequest(item2, 1L));
        mockMvc.perform(postRequest(bookingDto, 2L));
        mockMvc.perform(postRequest(bookingDto2, 2L));
    }

    @Test
    public void addBooking() throws Exception {
        BookingDto bookingDto = new BookingDto(null, getLTD(2), getLTD(3), null, 1L,
                null, null, null);
        mockMvc.perform(postRequest(bookingDto, 2L));

        Optional<Booking> foundBooking = repository.findById(3L);
        assertThat(foundBooking).isNotEmpty();
        assertThat(foundBooking.get().getId()).isEqualTo(3L);
        assertThat(foundBooking.get().getStart()).isEqualTo(bookingDto.getStart());
        assertThat(foundBooking.get().getEnd()).isEqualTo(bookingDto.getEnd());
        assertThat(foundBooking.get().getItem().getId()).isEqualTo(1L);
        assertThat(foundBooking.get().getBooker().getId()).isEqualTo(2L);
        assertThat(foundBooking.get().getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    public void setApprove() throws Exception {
        mockMvc.perform(patchRequest(1L, "true", 1L));

        Optional<Booking> foundBooking = repository.findById(1L);
        assertThat(foundBooking.get().getId()).isEqualTo(1L);
        assertThat(foundBooking.get().getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    public void getById() {
        Optional<Booking> foundBooking = repository.findById(1L);
        assertThat(foundBooking.get().getId()).isEqualTo(1L);
        assertThat(foundBooking.get().getItem().getId()).isEqualTo(1L);
        assertThat(foundBooking.get().getBooker().getId()).isEqualTo(2L);
    }

    @Test
    public void getAllForUser() {
        List<Booking> bookings = repository.findByBookerId(2L, Pageable.unpaged());
        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getId()).isEqualTo(1L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(1L);
        assertThat(bookings.get(1).getId()).isEqualTo(2L);
        assertThat(bookings.get(1).getItem().getId()).isEqualTo(2L);
    }

    @Test
    public void getAllForOwner() {
        List<Booking> bookings = repository.findByItemOwnerId(1L, Pageable.unpaged());
        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getId()).isEqualTo(1L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(1L);
        assertThat(bookings.get(1).getId()).isEqualTo(2L);
        assertThat(bookings.get(1).getItem().getId()).isEqualTo(2L);
    }

    private MockHttpServletRequestBuilder postRequest(UserDto user) throws JsonProcessingException {
        return MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(user));
    }

    private MockHttpServletRequestBuilder postRequest(ItemDto item,
                                                      Long ownerId) throws JsonProcessingException {
        return MockMvcRequestBuilders.post("/items")
                .header("X-Sharer-User-Id", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(item));
    }

    private MockHttpServletRequestBuilder postRequest(BookingDto booking,
                                                      Long userId) throws JsonProcessingException {
        return MockMvcRequestBuilders.post(url)
                .header("X-Sharer-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(booking));
    }

    private MockHttpServletRequestBuilder patchRequest(Long bookingId, String approve, Long ownerId) {
        return MockMvcRequestBuilders
                .patch(url + "/" + bookingId)
                .param("approved", approve)
                .header("X-Sharer-User-Id", ownerId);
    }

    private LocalDateTime getLTD(int sec) {
        return LocalDateTime.now().plusSeconds(sec).truncatedTo(ChronoUnit.SECONDS);
    }
}
