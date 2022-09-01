package ru.practicum.shareit.requests;

import lombok.*;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ItemRequest {

    private Long id;
    private String description;
    private User requestor;
    private LocalDateTime created;
}
