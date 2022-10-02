package ru.practicum.shareit.requests;

import ru.practicum.shareit.requests.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto add(Long userId, ItemRequestDto requestDto);

    List<ItemRequestDto> getAllByRequester(Long requesterId);

    List<ItemRequestDto> getAllExceptRequester(Long requesterId, Integer from, Integer size);

    ItemRequestDto getById(Long userId, Long requestId);

}
