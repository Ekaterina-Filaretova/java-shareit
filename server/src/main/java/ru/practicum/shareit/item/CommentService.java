package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;

import java.util.List;

public interface CommentService {

    CommentDto add(Long itemId, Long userId, CommentDto commentDto);

    List<CommentDto> getAllByItemId(Long itemId);
}
