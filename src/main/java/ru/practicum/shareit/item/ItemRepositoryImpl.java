package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
public class ItemRepositoryImpl implements ItemRepository {

    private final HashMap<Long, Item> items = new HashMap<>();

    @Override
    public Item addItem(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item getItemById(Long itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> getItemsByOwner(Long ownerId) {
        List<Item> userItems = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getOwner().getId().equals(ownerId)) {
                userItems.add(item);
            }
        }
        return userItems;
    }

    @Override
    public List<Item> getItemsBySearch(String text) {
        List<Item> foundItems = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getAvailable() &&
                    (item.getName().toLowerCase().contains(text) ||
                            item.getDescription().toLowerCase().contains(text))) {
                foundItems.add(item);
            }
        }
        return foundItems;
    }
}
