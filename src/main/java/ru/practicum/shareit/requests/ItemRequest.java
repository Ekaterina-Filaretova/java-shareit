package ru.practicum.shareit.requests;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "requests")
public class ItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;
    private String description;

    @JoinColumn(name = "requester_id")
    private Long requesterId;

    @Column(name = "create_date")
    private LocalDateTime created;
}
