package ru.practicum.shareit.booking;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping
    public BookingDto add(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @RequestBody BookingDto bookingDto) {
        return service.add(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto makeApproved(@RequestHeader("X-Sharer-User-Id") Long userId,
                                   @PathVariable Long bookingId,
                                   @RequestParam(value = "approved") boolean approved) {
        return service.makeApprove(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long bookingId) {
        return service.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getByUserAndState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestParam(value = "state") String state,
                                              @RequestParam(value = "from") Integer from,
                                              @RequestParam(value = "size") Integer size) {
        return service.getByUserAndState(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getByOwnerAndState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @RequestParam(value = "state") String state,
                                               @RequestParam(value = "from") Integer from,
                                               @RequestParam(value = "size") Integer size) {
        return service.getByOwnerAndState(userId, state, from, size);
    }
}