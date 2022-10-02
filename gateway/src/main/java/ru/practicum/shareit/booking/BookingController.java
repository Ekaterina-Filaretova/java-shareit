package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@Validated
public class BookingController {

    @Autowired
    private BookingClient client;

    @PostMapping
    public ResponseEntity<BookingDto> add(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @Valid @RequestBody BookingDto bookingDto) {
        return client.add(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> makeApproved(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @PathVariable Long bookingId,
                                                   @RequestParam(value = "approved") boolean approved) {
        return client.makeApprove(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable Long bookingId) {
        return client.getById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getByUserAndState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                              @RequestParam(value = "state", defaultValue = "ALL")
                                                              String state,
                                                              @RequestParam(value = "from", defaultValue = "0")
                                                              @PositiveOrZero Integer from,
                                                              @RequestParam(value = "size", defaultValue = "40")
                                                              @Positive Integer size) {
        return client.getByUserAndState(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDto>> getByOwnerAndState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                               @RequestParam(value = "state", defaultValue = "ALL")
                                                               String state,
                                                               @RequestParam(value = "from", defaultValue = "0")
                                                               @PositiveOrZero Integer from,
                                                               @RequestParam(value = "size", defaultValue = "40")
                                                               @Positive Integer size) {
        return client.getByOwnerAndState(userId, state, from, size);
    }
}
