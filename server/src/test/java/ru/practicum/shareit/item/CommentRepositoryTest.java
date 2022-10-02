package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/schema.sql")
@Sql(scripts = "/delete-data.sql", executionPhase = AFTER_TEST_METHOD)
public class CommentRepositoryTest {

    @Autowired
    private CommentRepository repository;

    @Autowired
    private TestEntityManager manager;

    @BeforeEach
    public void setUp() {
        User owner = new User(null, "user", "qwe@mail.com");
        User booker = new User(null, "user2", "asd@mail.com");
        Item item = new Item(null, "item", "desc", true, owner, null);
        Booking booking = new Booking(null, getLTD(2), getLTD(3), item, booker, BookingStatus.APPROVED);
        Comment comment = new Comment(null, "text", item, booker, getLTD(3));
        manager.persist(owner);
        manager.persist(booker);
        manager.persist(item);
        manager.persist(booking);
        manager.persist(comment);
    }

    @Test
    public void findAllByItemId() {
        List<Comment> comments = repository.findAllByItemId(1L);
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getId()).isEqualTo(1L);
        assertThat(comments.get(0).getText()).isEqualTo("text");
    }

    private LocalDateTime getLTD(int sec) {
        return LocalDateTime.now().plusSeconds(sec).truncatedTo(ChronoUnit.SECONDS);
    }
}
