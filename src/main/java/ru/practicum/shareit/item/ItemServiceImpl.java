package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository repository;
    private final UserService userService;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private Long id = 1L;

    public ItemServiceImpl(ItemRepository repository, UserService userService, UserMapper userMapper, ItemMapper itemMapper) {
        this.repository = repository;
        this.userService = userService;
        this.userMapper = userMapper;
        this.itemMapper = itemMapper;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, Long userId) {
        Item item = itemMapper.fromDto(itemDto);
        item.getOwner().setId(userId);
        if (isUserExist(item.getOwner().getId())) {
            item.setId(id++);
            item.setOwner(userMapper.fromDto(userService.getUserById(item.getOwner().getId())));
            log.info("Добавлен предмет {}", item);
            return itemMapper.toDto(repository.addItem(item));
        }
        throw new ObjectNotFoundException("Пользователь с id " + item.getOwner().getId() + " не найден");
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long userId, Long itemId) {
        Item item = itemMapper.fromDto(itemDto);
        item.setId(itemId);
        item.getOwner().setId(userId);
        for (Item i : repository.getItemsByOwner(item.getOwner().getId())) {
            if (i.getId().equals(item.getId())) {
                Item updatedItem = repository.getItemById(item.getId());
                if (item.getName() != null) {
                    updatedItem.setName(item.getName());
                }
                if (item.getDescription() != null) {
                    updatedItem.setDescription(item.getDescription());
                }
                if (item.getAvailable() != null) {
                    updatedItem.setAvailable(item.getAvailable());
                }
                log.info("Обновлен предмет {}", updatedItem);
                return itemMapper.toDto(repository.updateItem(updatedItem));
            }
        }
        throw new ObjectNotFoundException("У владельца " + userService.getUserById(item.getOwner().getId())
                + " нет предмета с id " + item.getId());
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = repository.getItemById(itemId);
        if (item != null) {
            log.info("Получен предмет {}", item);
            return itemMapper.toDto(item);
        }
        throw new ObjectNotFoundException("Предмет с id " + itemId + " не найден");
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        List<Item> userItems = repository.getItemsByOwner(ownerId);
        log.info("Получен список предметов {} пользователя {}", userItems, userService.getUserById(ownerId));
        List<ItemDto> itemDtos = new ArrayList<>();
        for (Item item : userItems) {
            itemDtos.add(itemMapper.toDto(item));
        }
        return itemDtos;
    }

    @Override
    public List<ItemDto> getItemsBySearch(String text) {
        text = text.toLowerCase().trim();
        List<Item> items = text.isEmpty() ? new ArrayList<>() : repository.getItemsBySearch(text);
        log.info("Получен список предметов {} по поиску {}", items, text);
        List<ItemDto> itemDtos = new ArrayList<>();
        for (Item item : items) {
            itemDtos.add(itemMapper.toDto(item));
        }
        return itemDtos;
    }

    private boolean isUserExist(Long userId) {
        for (UserDto user : userService.getAllUsers()) {
            if (user.getId().equals(userId)) {
                return true;
            }
        }
        return false;
    }
}
