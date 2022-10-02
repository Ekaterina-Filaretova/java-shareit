package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    private final String url = "/bookings";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookingService service;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void addBooking() throws Exception {
        BookingDto bookingDto = new BookingDto(1L, getLTD("+", 2), getLTD("+", 3),
                null, 1L, null, 1L, BookingStatus.WAITING);
        when(service.add(anyLong(), any())).thenReturn(bookingDto);

        mockMvc.perform(postRequest(bookingDto, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().toString())))
                .andExpect(jsonPath("$.itemId", is(bookingDto.getItemId()), Long.class))
                .andExpect(jsonPath("$.bookerId", is(bookingDto.getBookerId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));
    }

    @Test
    public void addBookingWithoutHeader() throws Exception {
        BookingDto bookingDto = new BookingDto(1L, getLTD("+", 2), getLTD("+", 3),
                null, 1L, null, 1L, BookingStatus.WAITING);

        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void makeApprove() throws Exception {
        BookingDto bookingDto = new BookingDto(1L, getLTD("+", 2), getLTD("+", 3),
                null, 1L, null, 1L, BookingStatus.APPROVED);
        when(service.makeApprove(anyLong(), anyLong(), anyBoolean())).thenReturn(bookingDto);

        mockMvc.perform(patchRequest(bookingDto.getId(), "true", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().toString())))
                .andExpect(jsonPath("$.itemId", is(bookingDto.getItemId()), Long.class))
                .andExpect(jsonPath("$.bookerId", is(bookingDto.getBookerId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));
    }

    @Test
    public void makeApproveWithoutHeader() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch(url + "/1")
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void makeApproveWithoutParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch(url + "/1")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getById() throws Exception {
        BookingDto bookingDto = new BookingDto(1L, getLTD("+", 2), getLTD("+", 3),
                null, 1L, null, 1L, BookingStatus.WAITING);
        when(service.getById(anyLong(), anyLong())).thenReturn(bookingDto);

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/" + bookingDto.getId())
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().toString())))
                .andExpect(jsonPath("$.itemId", is(bookingDto.getItemId()), Long.class))
                .andExpect(jsonPath("$.bookerId", is(bookingDto.getBookerId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));
    }

    @Test
    public void getByIdWithoutHeader() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url + "/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getByUserAndState() throws Exception {
        BookingDto bookingDto = new BookingDto(1L, getLTD("+", 2), getLTD("+", 3),
                null, 1L, null, 1L, BookingStatus.WAITING);
        when(service.getByUserAndState(any(), any(), any(), any())).thenReturn(List.of(bookingDto));

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(bookingDto.getEnd().toString())))
                .andExpect(jsonPath("$[0].itemId", is(bookingDto.getItemId()), Long.class))
                .andExpect(jsonPath("$[0].bookerId", is(bookingDto.getBookerId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(bookingDto.getStatus().toString())));
    }

    @Test
    public void getByUserAndStateWithoutHeader() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getByOwnerAndState() throws Exception {
        BookingDto bookingDto = new BookingDto(1L, getLTD("+", 2), getLTD("+", 3),
                null, 1L, null, 1L, BookingStatus.WAITING);
        when(service.getByOwnerAndState(any(), any(), any(), any())).thenReturn(List.of(bookingDto));

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(bookingDto.getEnd().toString())))
                .andExpect(jsonPath("$[0].itemId", is(bookingDto.getItemId()), Long.class))
                .andExpect(jsonPath("$[0].bookerId", is(bookingDto.getBookerId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(bookingDto.getStatus().toString())));
    }

    @Test
    public void getByOwnerAndStateWithoutHeader() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url + "/owner")
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());
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

    private LocalDateTime getLTD(String direction, int sec) {
        if (direction.equals("+")) {
            return LocalDateTime.now().plusSeconds(sec).truncatedTo(ChronoUnit.SECONDS);
        } else {
            return LocalDateTime.now().minusSeconds(sec).truncatedTo(ChronoUnit.SECONDS);
        }
    }
}
