package ru.practicum.shareit.item;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query(value = "SELECT * FROM items WHERE owner_id = ?1", nativeQuery = true)
    List<Item> findByOwnerId(Long ownerId, Pageable pageable);

    @Query(value = "SELECT * " +
            "FROM items " +
            "WHERE (POSITION(?1 IN LOWER(name)) > 0 OR POSITION(?1 IN LOWER(description)) > 0)" +
            "AND is_available", nativeQuery = true)
    List<Item> findByText(String text, Pageable pageable);

    List<Item> getAllByRequestId(Long requestId);
}
