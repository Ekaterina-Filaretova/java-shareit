package ru.practicum.shareit.requests;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.requests.dto.ItemRequestDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemRequestMapper {

    public ItemRequestDto convertToDto(ItemRequest request) {
        return new ItemRequestDto(request.getId(),
                request.getDescription(),
                request.getCreated(),
                new ArrayList<>());
    }

    public ItemRequest convertFromDto(ItemRequestDto requestDto) {
        return new ItemRequest(requestDto.getId(),
                requestDto.getDescription(),
                null,
                requestDto.getCreated());
    }

    public List<ItemRequestDto> convertToDto(List<ItemRequest> requests) {
        return requests
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}
