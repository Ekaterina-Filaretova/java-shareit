package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.utils.OffsetPageRequest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/schema.sql")
@Sql(scripts = "/delete-data.sql", executionPhase = AFTER_TEST_METHOD)
public class BookingRepositoryTest {

    @Autowired
    private BookingRepository repository;

    @Autowired
    private TestEntityManager manager;

    @BeforeEach
    public void setUp() {
        User owner = new User(null, "owner", "qwe@mail.com");
        User booker = new User(null, "booker", "asd@mail.com");
        Item item = new Item(null, "item", "desc", true, owner, null);
        Item item2 = new Item(null, "item2", "desc2", true, owner, null);
        Item item3 = new Item(null, "item3", "desc3", true, owner, null);
        Booking booking = new Booking(null, getLTD(2), getLTD(3), item, booker, BookingStatus.WAITING);
        Booking booking2 = new Booking(null, getLTD(2), getLTD(4), item2, booker, BookingStatus.APPROVED);
        Booking booking3 = new Booking(null, getLTD(3), getLTD(5), item3, booker, BookingStatus.REJECTED);
        manager.persist(owner);
        manager.persist(booker);
        manager.persist(item);
        manager.persist(item2);
        manager.persist(item3);
        manager.persist(booking);
        manager.persist(booking2);
        manager.persist(booking3);
    }

    @Test
    public void getByUserAndStateAll() {
        List<Booking> bookings = repository.findByBookerId(2L, Pageable.unpaged());
        assertThat(bookings).hasSize(3);
        assertThat(bookings.get(0).getId()).isEqualTo(1L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(1L);
        assertThat(bookings.get(1).getId()).isEqualTo(2L);
        assertThat(bookings.get(1).getItem().getId()).isEqualTo(2L);
        assertThat(bookings.get(2).getId()).isEqualTo(3L);
        assertThat(bookings.get(2).getItem().getId()).isEqualTo(3L);
    }

    @Test
    public void getByUserAndStateAllWithPagination() {
        List<Booking> bookings = repository.findByBookerId(2L,
                new OffsetPageRequest(1, 1, Sort.unsorted()));
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(2L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(2L);
    }

    @Test
    public void getByUserAndStateCurrent() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        List<Booking> bookings = repository.findByBookerIdAndStartIsBeforeAndEndIsAfter(2L, LocalDateTime.now(),
                LocalDateTime.now(), Pageable.unpaged());
        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getId()).isEqualTo(1L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(1L);
        assertThat(bookings.get(1).getId()).isEqualTo(2L);
        assertThat(bookings.get(1).getItem().getId()).isEqualTo(2L);
    }

    @Test
    public void getByUserAndStateCurrentWithPagination() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        List<Booking> bookings = repository.findByBookerIdAndStartIsBeforeAndEndIsAfter(2L, LocalDateTime.now(),
                LocalDateTime.now(), new OffsetPageRequest(1, 1, Sort.unsorted()));
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(2L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(2L);
    }

    @Test
    public void getByUserAndStatePast() {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        List<Booking> bookings = repository.findByBookerIdAndEndIsBefore(2L, LocalDateTime.now(),
                Pageable.unpaged());
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(1L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(1L);
    }

    @Test
    public void getByUserAndStatePastWithPagination() {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        List<Booking> bookings = repository.findByBookerIdAndEndIsBefore(2L, LocalDateTime.now(),
                new OffsetPageRequest(1, 1, Sort.unsorted()));
        assertThat(bookings).hasSize(0);
    }

    @Test
    public void getByUserAndStateFuture() {
        List<Booking> bookings = repository.findByBookerIdAndStartIsAfter(2L, LocalDateTime.now(),
                Pageable.unpaged());
        assertThat(bookings).hasSize(3);
        assertThat(bookings.get(0).getId()).isEqualTo(1L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(1L);
        assertThat(bookings.get(1).getId()).isEqualTo(2L);
        assertThat(bookings.get(1).getItem().getId()).isEqualTo(2L);
        assertThat(bookings.get(2).getId()).isEqualTo(3L);
        assertThat(bookings.get(2).getItem().getId()).isEqualTo(3L);
    }

    @Test
    public void getByUserAndStateFutureWithPagination() {
        List<Booking> bookings = repository.findByBookerIdAndStartIsAfter(2L, LocalDateTime.now(),
                new OffsetPageRequest(2, 1, Sort.unsorted()));
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(3L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(3L);
    }

    @Test
    public void getByUserAndStateWaiting() {
        List<Booking> bookings = repository.findByBookerIdAndStatusIs(2L, BookingStatus.WAITING,
                Pageable.unpaged());
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(1L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(1L);

    }

    @Test
    public void getByUserAndStateWaitingWithPagination() {
        List<Booking> bookings = repository.findByBookerIdAndStatusIs(2L, BookingStatus.WAITING,
                new OffsetPageRequest(1, 1, Sort.unsorted()));
        assertThat(bookings).hasSize(0);
    }

    @Test
    public void getByUserAndStateRejected() {
        List<Booking> bookings = repository.findByBookerIdAndStatusIs(2L, BookingStatus.REJECTED,
                Pageable.unpaged());
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(3L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(3L);

    }

    @Test
    public void getByUserAndStateRejectedWithPagination() {
        List<Booking> bookings = repository.findByBookerIdAndStatusIs(2L, BookingStatus.REJECTED,
                new OffsetPageRequest(0, 1, Sort.unsorted()));
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(3L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(3L);
    }

    @Test
    public void getBookingForComment() {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        Booking booking = repository.findByItemIdAndBookerIdAndEndIsBefore(1L, 2L,
                LocalDateTime.now());
        assertThat(booking.getId()).isEqualTo(1L);
        assertThat(booking.getItem().getId()).isEqualTo(1L);
    }

    @Test
    public void getByOwnerAndStateAll() {
        List<Booking> bookings = repository.findByItemOwnerId(1L, Pageable.unpaged());
        assertThat(bookings).hasSize(3);
        assertThat(bookings.get(0).getId()).isEqualTo(1L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(1L);
        assertThat(bookings.get(1).getId()).isEqualTo(2L);
        assertThat(bookings.get(1).getItem().getId()).isEqualTo(2L);
        assertThat(bookings.get(2).getId()).isEqualTo(3L);
        assertThat(bookings.get(2).getItem().getId()).isEqualTo(3L);
    }

    @Test
    public void getByOwnerAndStateAllWithPagination() {
        List<Booking> bookings = repository.findByItemOwnerId(1L,
                new OffsetPageRequest(1, 1, Sort.unsorted()));
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(2L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(2L);
    }

    @Test
    public void getByOwnerAndStateCurrent() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        List<Booking> bookings = repository.findByItemOwnerIdAndStartBeforeAndEndAfter(1L, LocalDateTime.now(),
                LocalDateTime.now(), Pageable.unpaged());
        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getId()).isEqualTo(1L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(1L);
        assertThat(bookings.get(1).getId()).isEqualTo(2L);
        assertThat(bookings.get(1).getItem().getId()).isEqualTo(2L);
    }

    @Test
    public void getByOwnerAndStateCurrentWithPagination() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        List<Booking> bookings = repository.findByItemOwnerIdAndStartBeforeAndEndAfter(1L, LocalDateTime.now(),
                LocalDateTime.now(), new OffsetPageRequest(1, 1, Sort.unsorted()));
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(2L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(2L);
    }

    @Test
    public void getByOwnerAndStatePast() {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        List<Booking> bookings = repository.findByItemOwnerIdAndEndIsBefore(1L, LocalDateTime.now(),
                Pageable.unpaged());
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(1L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(1L);
    }

    @Test
    public void getByOwnerAndStatePastWithPagination() {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        List<Booking> bookings = repository.findByItemOwnerIdAndEndIsBefore(1L, LocalDateTime.now(),
                new OffsetPageRequest(1, 1, Sort.unsorted()));
        assertThat(bookings).hasSize(0);
    }

    @Test
    public void getByOwnerAndStateFuture() {
        List<Booking> bookings = repository.findByItemOwnerIdAndStartIsAfter(1L, LocalDateTime.now(),
                Pageable.unpaged());
        assertThat(bookings).hasSize(3);
        assertThat(bookings.get(0).getId()).isEqualTo(1L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(1L);
        assertThat(bookings.get(1).getId()).isEqualTo(2L);
        assertThat(bookings.get(1).getItem().getId()).isEqualTo(2L);
        assertThat(bookings.get(2).getId()).isEqualTo(3L);
        assertThat(bookings.get(2).getItem().getId()).isEqualTo(3L);
    }

    @Test
    public void getByOwnerAndStateFutureWithPagination() {
        List<Booking> bookings = repository.findByItemOwnerIdAndStartIsAfter(1L, LocalDateTime.now(),
                new OffsetPageRequest(2, 1, Sort.unsorted()));
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(3L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(3L);
    }

    @Test
    public void getByOwnerAndStateWaiting() {
        List<Booking> bookings = repository.findByItemOwnerIdAndStatusIs(1L, BookingStatus.WAITING,
                Pageable.unpaged());
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(1L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(1L);

    }

    @Test
    public void getByOwnerAndStateWaitingWithPagination() {
        List<Booking> bookings = repository.findByItemOwnerIdAndStatusIs(1L, BookingStatus.WAITING,
                new OffsetPageRequest(1, 1, Sort.unsorted()));
        assertThat(bookings).hasSize(0);
    }

    @Test
    public void getByOwnerAndStateRejected() {
        List<Booking> bookings = repository.findByItemOwnerIdAndStatusIs(1L, BookingStatus.REJECTED,
                Pageable.unpaged());
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(3L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(3L);

    }

    @Test
    public void getByOwnerAndStateRejectedWithPagination() {
        List<Booking> bookings = repository.findByItemOwnerIdAndStatusIs(1L, BookingStatus.REJECTED,
                new OffsetPageRequest(0, 1, Sort.unsorted()));
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(3L);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(3L);
    }

    @Test
    public void getLastBooking() {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        Booking booking = repository.findFirstByItemIdAndEndIsBefore(1L, LocalDateTime.now(), Sort.unsorted());
        assertThat(booking.getId()).isEqualTo(1L);
        assertThat(booking.getItem().getId()).isEqualTo(1L);
    }

    @Test
    public void getNextBooking() {
        Booking booking = repository.findFirstByItemIdAndStartIsAfter(2L, LocalDateTime.now(), Sort.unsorted());
        assertThat(booking.getId()).isEqualTo(2L);
        assertThat(booking.getItem().getId()).isEqualTo(2L);
    }

    private LocalDateTime getLTD(int sec) {
        return LocalDateTime.now().plusSeconds(sec).truncatedTo(ChronoUnit.SECONDS);
    }
}
