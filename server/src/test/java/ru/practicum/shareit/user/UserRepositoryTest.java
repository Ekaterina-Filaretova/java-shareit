package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/schema.sql")
@Sql(scripts = "/delete-data.sql", executionPhase = AFTER_TEST_METHOD)
public class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Autowired
    private TestEntityManager manager;

    @BeforeEach
    public void setUp() {
        User user = new User(null, "user", "qwe@mail.com");
        User user2 = new User(null, "user2", "asd@mail.com");
        manager.persist(user);
        manager.persist(user2);
    }

    @Test
    public void addUser() {
        Optional<User> foundUser = repository.findById(1L);
        assertThat(foundUser.get().getId()).isEqualTo(1L);
        assertThat(foundUser.get().getName()).isEqualTo("user");
        assertThat(foundUser.get().getEmail()).isEqualTo("qwe@mail.com");
    }

    @Test
    public void addUserWithExistMail() {
        User user = new User(null, "user", "qwe@mail.com");

        assertThatThrownBy(() ->
                repository.save(user))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void updateUser() {
        User user = new User(1L, "new user", "asd@mail.com");
        repository.save(user);

        Optional<User> foundUser = repository.findById(1L);
        assertThat(foundUser.get()).usingRecursiveComparison().isEqualTo(user);
    }

    @Test
    public void getAll() {
        List<User> users = repository.findAll();
        assertThat(users).hasSize(2);
        assertThat(users.get(0).getId()).isEqualTo(1L);
        assertThat(users.get(0).getName()).isEqualTo("user");
        assertThat(users.get(0).getEmail()).isEqualTo("qwe@mail.com");

        assertThat(users.get(1).getId()).isEqualTo(2L);
        assertThat(users.get(1).getName()).isEqualTo("user2");
        assertThat(users.get(1).getEmail()).isEqualTo("asd@mail.com");
    }

    @Test
    public void delete() {
        repository.deleteById(1L);
        Optional<User> foundUser = repository.findById(1L);
        assertThat(foundUser).isEmpty();
    }
}
