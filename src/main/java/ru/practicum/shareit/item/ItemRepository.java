package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerIdOrderByIdAsc(Long ownerId);

    @Query(value = "SELECT * " +
            "FROM items AS it " +
            "WHERE (POSITION(?1 IN LOWER(it.name)) > 0 OR POSITION(?1 IN LOWER(it.description)) > 0)" +
            "AND it.is_available", nativeQuery = true)
    List<Item> findByText(String text);
}
