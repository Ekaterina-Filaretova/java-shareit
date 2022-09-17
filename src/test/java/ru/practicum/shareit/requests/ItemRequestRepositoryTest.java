package ru.practicum.shareit.requests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.utils.OffsetPageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/schema.sql")
@Sql(scripts = "/delete-data.sql", executionPhase = AFTER_TEST_METHOD)
public class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository repository;

    @Autowired
    private TestEntityManager manager;

    @BeforeEach
    public void setUp() {
        User requester = new User(null, "requester", "qwe@mail.com");
        User requester2 = new User(null, "requester2", "asd@mail.com");
        ItemRequest request = new ItemRequest(null, "text", requester, LocalDateTime.now());
        ItemRequest request2 = new ItemRequest(null, "text2", requester2, LocalDateTime.now());
        ItemRequest request3 = new ItemRequest(null, "text3", requester2, LocalDateTime.now());
        manager.persist(requester);
        manager.persist(requester2);
        manager.persist(request);
        manager.persist(request2);
        manager.persist(request3);
    }

    @Test
    public void findAllByRequesterId() {
        List<ItemRequest> requests = repository.findAllByRequesterId(1L, Sort.unsorted());

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getId()).isEqualTo(1L);
        assertThat(requests.get(0).getDescription()).isEqualTo("text");
    }

    @Test
    public void findAllByRequesterIdNot() {
        List<ItemRequest> requests = repository.findAllByRequesterIdNot(1L, Pageable.unpaged());

        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getId()).isEqualTo(2L);
        assertThat(requests.get(0).getDescription()).isEqualTo("text2");
        assertThat(requests.get(1).getId()).isEqualTo(3L);
        assertThat(requests.get(1).getDescription()).isEqualTo("text3");
    }

    @Test
    public void findAllByRequesterIdNotWithPagination() {
        List<ItemRequest> requests = repository.findAllByRequesterIdNot(1L,
                new OffsetPageRequest(1, 1, Sort.unsorted()));

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getId()).isEqualTo(3L);
        assertThat(requests.get(0).getDescription()).isEqualTo("text3");
    }
}
