package ru.practicum.shareit.item;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;
    private final ItemMapper itemMapper;


    public ItemController(ItemService itemService, ItemMapper itemMapper) {
        this.itemService = itemService;
        this.itemMapper = itemMapper;
    }

    @PostMapping
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @Valid @RequestBody ItemDto itemDto) {
        Item item = itemMapper.fromDto(itemDto);
        item.getOwner().setId(userId);
        item = itemService.addItem(item);
        return itemMapper.toDto(item);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody ItemDto itemDto,
                              @PathVariable Long itemId) {
        Item item = itemMapper.fromDto(itemDto);
        item.getOwner().setId(userId);
        item.setId(itemId);
        item = itemService.updateItem(item);
        return itemMapper.toDto(item);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId) {
        Item item = itemService.getItemById(itemId);
        return itemMapper.toDto(item);
    }

    @GetMapping
    public List<ItemDto> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        List<ItemDto> items = new ArrayList<>();
        for (Item item : itemService.getItemsByOwner(userId)) {
            items.add(itemMapper.toDto(item));
        }
        return items;
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsBySearch(@RequestParam(required = false) String text) {
        List<ItemDto> items = new ArrayList<>();
        for (Item item : itemService.getItemsBySearch(text)) {
            items.add(itemMapper.toDto(item));
        }
        return items;
    }

}
