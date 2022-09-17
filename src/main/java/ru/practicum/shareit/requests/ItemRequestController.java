package ru.practicum.shareit.requests;

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

    private final ItemRequestService service;

    public ItemRequestController(ItemRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ItemRequestDto add(@RequestHeader("X-Sharer-User-Id") Long requesterId,
                              @Valid @RequestBody ItemRequestDto requestDto) {
        return service.add(requesterId, requestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getAllByRequester(@RequestHeader("X-Sharer-User-Id") Long requesterId) {
        return service.getAllByRequester(requesterId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllExceptRequester(@RequestHeader("X-Sharer-User-Id") Long requesterId,
                                                      @RequestParam(value = "from", defaultValue = "0")
                                                      @PositiveOrZero Integer from,
                                                      @RequestParam(value = "size", defaultValue = "40")
                                                      @Positive Integer size) {
        return service.getAllExceptRequester(requesterId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                  @PathVariable Long requestId) {
        return service.getById(userId, requestId);
    }
}
