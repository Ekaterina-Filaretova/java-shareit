package ru.practicum.shareit.requests;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ItemRequest {

    private Long id;
    private String description;
    private Long requesterId;
    private LocalDateTime created;
}
