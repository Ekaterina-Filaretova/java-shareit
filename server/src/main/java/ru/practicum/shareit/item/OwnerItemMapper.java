package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;

import java.util.ArrayList;
import java.util.List;

@Component
public class OwnerItemMapper {

    public ItemWithBookingDto toDto(Item item) {
        return new ItemWithBookingDto(item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                null,
                null,
                new ArrayList<>(),
                item.getRequest());
    }

    public List<ItemWithBookingDto> toDto(List<Item> items) {
        List<ItemWithBookingDto> itemDtos = new ArrayList<>();
        for (Item item : items) {
            itemDtos.add(toDto(item));
        }
        return itemDtos;
    }
}