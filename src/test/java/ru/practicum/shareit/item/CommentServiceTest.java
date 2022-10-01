package ru.practicum.shareit.item;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @InjectMocks
    private CommentServiceImpl commentService;

    @Mock
    private CommentRepository repository;

    @Mock
    private UserService userService;

    @Mock
    private ItemService itemService;

    @Mock
    private BookingService bookingService;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private UserMapper userMapper;

    @Test
    public void addComment() {
        CommentDto commentDto = new CommentDto(1L, "text", "user", LocalDateTime.now());
        Item item = new Item(1L, "item", "desc", true, new User(), null);
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(itemService.getItemById(any())).thenReturn(item);
        when(bookingService.getByItemId(any(), any(), any())).thenReturn(new Booking());
        when(commentMapper.fromDto(commentDto)).thenReturn(new Comment());
        when(userMapper.fromDto(any())).thenReturn(new User(1L, "user", "qwe@mail.com"));
        when(repository.save(any())).thenReturn(new Comment());
        when(commentMapper.toDto(any())).thenReturn(commentDto);

        CommentDto savedComment = commentService.add(1L, 1L, commentDto);
        assertThat(savedComment).usingRecursiveComparison().isEqualTo(commentDto);
    }

    @Test
    public void addCommentWithoutUser() {
        when(userService.getUserById(anyLong())).thenReturn(null);

        assertThatThrownBy(() ->
                commentService.add(1L, 1L, new CommentDto()))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void addCommentWithoutItem() {
        when(userService.getUserById(anyLong())).thenReturn(new UserDto());
        when(itemService.getItemById(anyLong())).thenReturn(null);

        assertThatThrownBy(() ->
                commentService.add(1L, 1L, new CommentDto()))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void addCommentWithoutBooking() {
        when(userService.getUserById(anyLong())).thenReturn(new UserDto());
        when(itemService.getItemById(anyLong())).thenReturn(new Item());
        when(bookingService.getByItemId(any(), any(), any())).thenReturn(null);

        assertThatThrownBy(() ->
                commentService.add(1L, 1L, new CommentDto()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void getAllByItemId() {
        when(repository.findAllByItemId(anyLong())).thenReturn(List.of(new Comment(), new Comment()));
        when(commentMapper.toDto(any())).thenReturn(new CommentDto());

        List<CommentDto> comments = commentService.getAllByItemId(1L);
        assertThat(comments).hasSize(2);
    }

    @Test
    public void getAllByItemIdWithoutItem() {
        List<CommentDto> comments = commentService.getAllByItemId(null);
        assertThat(comments).hasSize(0);
    }
}
