package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository repository;
    private final UserService userService;
    private final BookingService bookingService;
    private final CommentService commentService;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final OwnerItemMapper ownerItemMapper;

    public ItemServiceImpl(ItemRepository repository,
                           UserService userService,
                           @Lazy BookingService bookingService,
                           @Lazy CommentService commentService,
                           UserMapper userMapper,
                           ItemMapper itemMapper,
                           OwnerItemMapper ownerItemMapper) {
        this.repository = repository;
        this.userService = userService;
        this.bookingService = bookingService;
        this.commentService = commentService;
        this.userMapper = userMapper;
        this.itemMapper = itemMapper;
        this.ownerItemMapper = ownerItemMapper;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, Long userId) {
        Item item = itemMapper.fromDto(itemDto);
        item.getOwner().setId(userId);
        if (userService.getUserById(userId) == null) {
            throw new ObjectNotFoundException("Пользователь с id " + item.getOwner().getId() + " не найден");
        }
        item.setOwner(userMapper.fromDto(userService.getUserById(item.getOwner().getId())));
        log.info("Добавлен предмет {}", item);
        return itemMapper.toDto(repository.save(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long userId, Long itemId) {
        for (Item ownerItem : repository.findByOwnerIdOrderByIdAsc(userId)) {
            if (ownerItem.getId().equals(itemId)) {
                Item updatedItem = getItemById(itemId);
                if (itemDto.getName() != null) {
                    updatedItem.setName(itemDto.getName());
                }
                if (itemDto.getDescription() != null) {
                    updatedItem.setDescription(itemDto.getDescription());
                }
                if (itemDto.getAvailable() != null) {
                    updatedItem.setAvailable(itemDto.getAvailable());
                }
                log.info("Обновлен предмет {}", updatedItem);
                return itemMapper.toDto(repository.save(updatedItem));
            }
        }
        throw new ObjectNotFoundException("У владельца с id " + userId + " нет предмета с id " + itemId);
    }

    @Override
    public ItemWithBookingDto getItemById(Long userId, Long itemId) {
        Optional<Item> item = repository.findById(itemId);
        if (item.isEmpty()) {
            throw new ObjectNotFoundException("Предмет с id " + itemId + " не найден");
        }
        ItemWithBookingDto itemDto = ownerItemMapper.toDto(item.get());
        itemDto.setComments(commentService.getAllByItemId(itemId));
        if (item.get().getOwner().getId().equals(userId)) {
            itemDto.setLastBooking(bookingService.getLastBooking(itemId));
            itemDto.setNextBooking(bookingService.getNextBooking(itemId));
        }
        log.info("Получен предмет {}", itemDto);
        return itemDto;
    }

    @Override
    public List<ItemWithBookingDto> getItemsByOwner(Long ownerId) {
        List<ItemWithBookingDto> items = ownerItemMapper.toDto(repository.findByOwnerIdOrderByIdAsc(ownerId));
        for (ItemWithBookingDto item : items) {
            item.setLastBooking(bookingService.getLastBooking(item.getId()));
            item.setNextBooking(bookingService.getNextBooking(item.getId()));
        }
        log.info("Получен список предметов {} пользователя {}", items, userService.getUserById(ownerId));
        return items;
    }

    @Override
    public List<ItemDto> getItemsBySearch(String text) {
        text = text.toLowerCase().trim();
        List<Item> items = text.isEmpty() ? new ArrayList<>() : repository.findByText(text);
        log.info("Получен список предметов {} по поиску {}", items, text);
        return itemMapper.toDto(items);
    }

    @Override
    public Item getItemById(Long itemId) {
        Optional<Item> item = repository.findById(itemId);
        if (item.isEmpty()) {
            throw new ObjectNotFoundException("Предмет с id " + itemId + " не найден");
        }
        item.get().setOwner(userMapper.fromDto(userService.getUserById(item.get().getOwner().getId())));
        return item.get();
    }

    @Override
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        return commentService.add(itemId, userId, commentDto);
    }
}