package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;

import java.util.List;

public interface ItemService {

    ItemDto addItem(ItemDto item, Long userId);

    ItemDto updateItem(ItemDto item, Long userId, Long itemId);

    ItemWithBookingDto getItemById(Long userId, Long itemId);

    List<ItemWithBookingDto> getItemsByOwner(Long ownerId);

    List<ItemDto> getItemsBySearch(String text);

    Item getItemById(Long itemId);

    CommentDto addComment(Long itemId, Long userId, CommentDto commentDto);
}
