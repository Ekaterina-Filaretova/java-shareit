package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository repository;
    private final UserService userService;
    private Long id = 1L;

    public ItemServiceImpl(ItemRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    @Override
    public Item addItem(Item item) {
        if (isUserExist(item.getOwner().getId())) {
            item.setId(id++);
            item.setOwner(userService.getUserById(item.getOwner().getId()));
            log.info("Добавлен предмет {}", item);
            return repository.addItem(item);
        }
        throw new ObjectNotFoundException("Пользователь с id " + item.getOwner().getId() + " не найден");
    }

    @Override
    public Item updateItem(Item item) {
        for (Item i : getItemsByOwner(item.getOwner().getId())) {
            if (i.getId().equals(item.getId())) {
                Item updatedItem = getItemById(item.getId());
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
                return repository.updateItem(updatedItem);
            }
        }
        throw new ObjectNotFoundException("У владельца " + userService.getUserById(item.getOwner().getId())
                + " нет предмета с id " + item.getId());
    }

    @Override
    public Item getItemById(Long itemId) {
        Item item = repository.getItemById(itemId);
        log.info("Получен предмет {}", item);
        return item;
    }

    @Override
    public List<Item> getItemsByOwner(Long ownerId) {
        List<Item> userItems = repository.getItemsByOwner(ownerId);
        log.info("Получен список предметов {} пользователя {}", userItems, userService.getUserById(ownerId));
        return userItems;
    }

    @Override
    public List<Item> getItemsBySearch(String text) {
        text = text.toLowerCase().trim();
        List<Item> items = text.isEmpty() ? new ArrayList<>() : repository.getItemsBySearch(text);
        log.info("Получен список предметов {} по поиску {}", items, text);
        return items;
    }

    private boolean isUserExist(Long userId) {
        for (User user : userService.getAllUsers()) {
            if (user.getId().equals(userId)) {
                return true;
            }
        }
        return false;
    }
}
