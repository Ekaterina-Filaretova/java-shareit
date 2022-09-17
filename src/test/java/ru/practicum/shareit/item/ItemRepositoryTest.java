package ru.practicum.shareit.item;

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
import ru.practicum.shareit.requests.ItemRequest;
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
public class ItemRepositoryTest {

    @Autowired
    private ItemRepository repository;

    @Autowired
    private TestEntityManager manager;

    @BeforeEach
    public void setUp() {
        User user = new User(null, "user", "qwe@mail.com");
        User user2 = new User(null, "user2", "asd@mail.com");
        ItemRequest request = new ItemRequest(null, "desc", user2, LocalDateTime.now());
        Item item = new Item(null, "item", "desc", true, user, request);
        Item item2 = new Item(null, "second item", "desc2", true, user, null);
        Item item3 = new Item(null, "third item", "desc3", true, user2, null);
        manager.persist(user);
        manager.persist(user2);
        manager.persist(request);
        manager.persist(item);
        manager.persist(item2);
        manager.persist(item3);
    }

    @Test
    public void getByOwnerId() {
        List<Item> items = repository.findByOwnerId(2L, Pageable.unpaged());
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getId()).isEqualTo(3L);
        assertThat(items.get(0).getName()).isEqualTo("third item");
        assertThat(items.get(0).getDescription()).isEqualTo("desc3");
        assertThat(items.get(0).getOwner().getName()).isEqualTo("user2");
    }

    @Test
    public void getByOwnerIdWithPagination() {
        List<Item> items = repository.findByOwnerId(1L, new OffsetPageRequest(1, 1, Sort.unsorted()));
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getId()).isEqualTo(2L);
        assertThat(items.get(0).getName()).isEqualTo("second item");
        assertThat(items.get(0).getDescription()).isEqualTo("desc2");
    }

    @Test
    public void searchByTextInName() {
        List<Item> items = repository.findByText("sec", Pageable.unpaged());
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getId()).isEqualTo(2L);
        assertThat(items.get(0).getName()).isEqualTo("second item");
        assertThat(items.get(0).getDescription()).isEqualTo("desc2");
    }

    @Test
    public void searchByTextInDescription() {
        List<Item> items = repository.findByText("ird", Pageable.unpaged());
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getId()).isEqualTo(3L);
        assertThat(items.get(0).getName()).isEqualTo("third item");
        assertThat(items.get(0).getDescription()).isEqualTo("desc3");
    }

    @Test
    public void searchByTextWithPagination() {
        List<Item> items = repository.findByText("it", new OffsetPageRequest(2, 2, Sort.unsorted()));
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getId()).isEqualTo(3L);
        assertThat(items.get(0).getName()).isEqualTo("third item");
        assertThat(items.get(0).getDescription()).isEqualTo("desc3");
    }

    @Test
    public void getAllByRequestId() {
        List<Item> items = repository.getAllByRequestId(1L);
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getId()).isEqualTo(1L);
        assertThat(items.get(0).getName()).isEqualTo("item");
        assertThat(items.get(0).getDescription()).isEqualTo("desc");
    }
}
