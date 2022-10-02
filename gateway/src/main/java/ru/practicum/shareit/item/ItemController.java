package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/items")
@Validated
public class ItemController {

    @Autowired
    private ItemClient client;

    @PostMapping
    public ResponseEntity<ItemDto> addItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @Valid @RequestBody ItemDto itemDto) {
        return client.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestBody ItemDto itemDto,
                                              @PathVariable Long itemId) {
        return client.updateItem(itemId, userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemWithBookingDto> getById(@PathVariable Long itemId,
                                                      @RequestHeader("X-Sharer-User-Id") Long userId) {
        return client.getItemById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<List<ItemWithBookingDto>> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                    @RequestParam(value = "from", defaultValue = "0")
                                                                    @PositiveOrZero Integer from,
                                                                    @RequestParam(value = "size", defaultValue = "40")
                                                                    @Positive Integer size) {
        return client.getItemsByOwner(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> getItemsBySearch(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                          @RequestParam(required = false) String text,
                                                          @RequestParam(value = "from", defaultValue = "0")
                                                          @PositiveOrZero Integer from,
                                                          @RequestParam(value = "size", defaultValue = "40")
                                                          @Positive Integer size) {
        return client.getItemsBySearch(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(@PathVariable Long itemId,
                                                 @RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @Valid @RequestBody CommentDto commentDto) {
        return client.addComment(itemId, userId, commentDto);
    }
}
