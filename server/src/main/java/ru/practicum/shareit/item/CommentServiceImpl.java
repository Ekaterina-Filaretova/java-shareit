package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository repository;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    public CommentServiceImpl(CommentRepository repository,
                              UserService userService,
                              ItemService itemService,
                              BookingService bookingService,
                              CommentMapper commentMapper,
                              UserMapper userMapper) {
        this.repository = repository;
        this.userService = userService;
        this.itemService = itemService;
        this.bookingService = bookingService;
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
    }

    @Override
    public CommentDto add(Long itemId, Long userId, CommentDto commentDto) {
        if (userService.getUserById(userId) == null) {
            throw new ObjectNotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (itemService.getItemById(itemId) == null) {
            throw new ObjectNotFoundException("Предмет с id " + itemId + " не найден");
        }
        if (bookingService.getByItemId(itemId, userId, LocalDateTime.now()) == null) {
            throw new ValidationException("Бронирование предмета с id " + itemId + " не найдено");
        }
        Comment comment = commentMapper.fromDto(commentDto);
        comment.setAuthor(userMapper.fromDto(userService.getUserById(userId)));
        comment.setItem(itemService.getItemById(itemId));
        comment.setCreated(LocalDateTime.now());
        log.info("Добавлен комментарий {}", comment);
        return commentMapper.toDto(repository.save(comment));
    }

    @Override
    public List<CommentDto> getAllByItemId(Long itemId) {
        List<CommentDto> commentDtos = new ArrayList<>();
        for (Comment comment : repository.findAllByItemId(itemId)) {
            commentDtos.add(commentMapper.toDto(comment));
        }
        return commentDtos;
    }
}