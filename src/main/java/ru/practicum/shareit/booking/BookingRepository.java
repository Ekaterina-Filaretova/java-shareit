package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    String request = "SELECT booking_id, b.start_date, b.end_date, b.item_id, b.booker_id, b.status " +
            "FROM items i " +
            "LEFT JOIN bookings b ON i.item_id = b.item_id " +
            "WHERE i.owner_id = ?1 ";

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long bookerId,
                                                              LocalDateTime before,
                                                              LocalDateTime after,
                                                              Sort sort);

    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime time, Sort sort);

    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime time, Sort sort);

    List<Booking> findByBookerIdAndStatusIs(Long bookerId, BookingStatus status, Sort sort);

    @Query(name = "SELECT *" +
            "FROM bookings b " +
            "WHERE b.item_id = ?1 AND b.booker_id = ?2 AND b.end_date < ?3 " +
            "AND (b.status LIKE 'APPROVED' OR b.status LIKE 'WAITING') " +
            "LIMIT 1", nativeQuery = true)
    Booking findByItemIdAndBookerIdAndEndIsBefore(Long itemId, Long userId, LocalDateTime time);

    @Query(name = request + " AND (b.status LIKE 'WAITING' OR b.status LIKE 'APPROVED') ",
            nativeQuery = true)
    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(Long ownerId,
                                                                 LocalDateTime before,
                                                                 LocalDateTime after,
                                                                 Sort sort);

    @Query(name = request + "AND (b.status LIKE 'WAITING' OR b.status LIKE 'APPROVED') " +
            "AND b.end_date < ?2 ",
            nativeQuery = true)
    List<Booking> findByItemOwnerIdAndEndIsBefore(Long ownerId, LocalDateTime time, Sort sort);

    @Query(name = request + "AND (b.status LIKE 'WAITING' OR b.status LIKE 'APPROVED') " +
            "AND b.start_date > ?2 ",
            nativeQuery = true)
    List<Booking> findByItemOwnerIdAndStartIsAfter(Long ownerId, LocalDateTime time, Sort sort);

    List<Booking> findByItemOwnerIdAndStatusIs(Long ownerId, BookingStatus status, Sort sort);

    Booking findFirstByItemIdAndEndIsBefore(Long itemId, LocalDateTime time, Sort sort);

    Booking findFirstByItemIdAndStartIsAfter(Long itemId, LocalDateTime time, Sort sort);
}
