package ru.practicum.shareit.requests;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.requests.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {

    private final ItemRequestClient client;

    public ItemRequestController(ItemRequestClient client) {
        this.client = client;
    }

    @PostMapping
    public ResponseEntity<ItemRequestDto> add(@RequestHeader("X-Sharer-User-Id") Long requesterId,
                                              @Valid @RequestBody ItemRequestDto requestDto) {
        return client.add(requesterId, requestDto);
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDto>> getAllByRequester(@RequestHeader("X-Sharer-User-Id") Long requesterId) {
        return client.getAllByRequester(requesterId);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestDto>> getAllExceptRequester(@RequestHeader("X-Sharer-User-Id")
                                                                      Long requesterId,
                                                                      @RequestParam(value = "from", defaultValue = "0")
                                                                      @PositiveOrZero Integer from,
                                                                      @RequestParam(value = "size", defaultValue = "40")
                                                                      @Positive Integer size) {
        return client.getAllExceptRequester(requesterId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDto> getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @PathVariable Long requestId) {
        return client.getById(userId, requestId);
    }
}
