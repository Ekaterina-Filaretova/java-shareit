package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ErrorHandler;

import java.util.List;

@Component
public class BookingClient {

    private final RestTemplate template;

    public BookingClient(@Value("${shareit-server.url}/bookings") String url, RestTemplateBuilder template) {
        this.template = template
                .errorHandler(new ErrorHandler())
                .uriTemplateHandler(new DefaultUriBuilderFactory(url))
                .build();
    }

    public ResponseEntity<BookingDto> add(Long userId, BookingDto bookingDto) {
        return template.postForEntity("",
                getHttpEntity(userId, bookingDto),
                BookingDto.class);
    }

    public ResponseEntity<BookingDto> makeApprove(Long userId, Long bookingId, boolean approved) {
        return template.exchange("/{bookingId}?approved={approved}",
                HttpMethod.PATCH,
                getHttpEntity(userId, null),
                BookingDto.class,
                bookingId, approved);
    }

    public ResponseEntity<BookingDto> getById(Long userId, Long bookingId) {
        return template.exchange("/{bookingId}",
                HttpMethod.GET,
                getHttpEntity(userId, null),
                BookingDto.class,
                bookingId);
    }

    public ResponseEntity<List<BookingDto>> getByUserAndState(Long userId, String state, Integer from, Integer size) {
        return template.exchange("?state={state}&from={from}&size={size}",
                HttpMethod.GET,
                getHttpEntity(userId, null),
                new ParameterizedTypeReference<>() {
                },
                state, from, size);
    }

    public ResponseEntity<List<BookingDto>> getByOwnerAndState(Long userId, String state, Integer from, Integer size) {
        return template.exchange("/owner?state={state}&from={from}&size={size}",
                HttpMethod.GET,
                getHttpEntity(userId, null),
                new ParameterizedTypeReference<>() {
                },
                state, from, size);
    }

    private HttpEntity<BookingDto> getHttpEntity(Long userId, BookingDto bookingDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(userId));
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return bookingDto == null ? new HttpEntity<>(headers) : new HttpEntity<>(bookingDto, headers);
    }
}
