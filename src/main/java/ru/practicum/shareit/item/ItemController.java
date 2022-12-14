package ru.practicum.shareit.item;

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

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @Valid @RequestBody ItemDto itemDto) {
        return itemService.addItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody ItemDto itemDto,
                              @PathVariable Long itemId) {
        return itemService.updateItem(itemDto, userId, itemId);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingDto getById(@PathVariable Long itemId,
                                      @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemWithBookingDto> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @RequestParam(value = "from", defaultValue = "0")
                                                    @PositiveOrZero Integer from,
                                                    @RequestParam(value = "size", defaultValue = "40")
                                                    @Positive Integer size) {
        return itemService.getItemsByOwner(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsBySearch(@RequestParam(required = false) String text,
                                          @RequestParam(value = "from", defaultValue = "0")
                                          @PositiveOrZero Integer from,
                                          @RequestParam(value = "size", defaultValue = "40")
                                          @Positive Integer size) {
        return itemService.getItemsBySearch(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable Long itemId,
                                 @RequestHeader("X-Sharer-User-Id") Long userId,
                                 @Valid @RequestBody CommentDto commentDto) {
        return itemService.addComment(itemId, userId, commentDto);
    }
}
