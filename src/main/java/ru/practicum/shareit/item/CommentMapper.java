package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.user.User;

@Component
public class CommentMapper {

    public Comment fromDto(CommentDto comment) {
        return new Comment(comment.getId(),
                comment.getText(),
                new Item(),
                new User(),
                null);
    }

    public CommentDto toDto(Comment comment) {
        return new CommentDto(comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated());
    }
}
