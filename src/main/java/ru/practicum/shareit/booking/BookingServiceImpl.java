package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.utils.OffsetPageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository repository;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingMapper bookingMapper;
    private final UserMapper userMapper;

    public BookingServiceImpl(BookingRepository repository,
                              ItemService itemService,
                              UserService userService,
                              BookingMapper bookingMapper,
                              UserMapper userMapper) {
        this.repository = repository;
        this.itemService = itemService;
        this.userService = userService;
        this.bookingMapper = bookingMapper;
        this.userMapper = userMapper;
    }

    @Override
    public BookingDto add(Long userId, BookingDto bookingDto) {
        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new ValidationException("Дата конца аренды не может быть раньше даты начала аренды");
        }
        Booking booking = bookingMapper.fromDto(bookingDto);
        booking.setItem(itemService.getItemById(bookingDto.getItemId()));
        if (booking.getItem().getOwner().getId().equals(userId)) {
            throw new ObjectNotFoundException("Владелец не может арендовать собственную вещь");
        }
        if (!booking.getItem().getAvailable()) {
            throw new ValidationException("Предмет " + booking.getItem() + " не доступен");
        }
        booking.setBooker(userMapper.fromDto(userService.getUserById(userId)));
        log.info("Добавлено бронирование {}", booking);
        return bookingMapper.toDto(repository.save(booking));
    }

    @Override
    public BookingDto makeApprove(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = checkBooking(bookingId);
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ObjectNotFoundException("Изменить статус бронирования может только владелец предмета");
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ValidationException("Изменить статус бронирования невозможно");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        log.info("Бронированию {} установлен новый статус {}", booking, booking.getStatus());
        return bookingMapper.toDto(repository.save(booking));
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = checkBooking(bookingId);
        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ObjectNotFoundException("Просмотреть бронирование может либо владелец вещи, либо автор аренды");
        }
        log.info("Получено бронирование {}", booking);
        return bookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getByUserAndState(Long bookerId, String state, Integer from, Integer size) {
        checkEnum(state);
        List<BookingDto> bookings = new ArrayList<>();
        if (userService.getUserById(bookerId) != null) {
            switch (BookingState.valueOf(state)) {
                case ALL:
                    bookings = bookingMapper.toDto(repository.findByBookerId(bookerId, getPagination(from, size)));
                    break;
                case CURRENT:
                    bookings = bookingMapper.toDto(repository.findByBookerIdAndStartIsBeforeAndEndIsAfter(
                            bookerId, LocalDateTime.now(), LocalDateTime.now(), getPagination(from, size)));
                    break;
                case PAST:
                    bookings = bookingMapper.toDto(repository.findByBookerIdAndEndIsBefore(
                            bookerId, LocalDateTime.now(), getPagination(from, size)));
                    break;
                case FUTURE:
                    bookings = bookingMapper.toDto(repository.findByBookerIdAndStartIsAfter(
                            bookerId, LocalDateTime.now(), getPagination(from, size)));
                    break;
                case WAITING:
                    bookings = bookingMapper.toDto(repository.findByBookerIdAndStatusIs(
                            bookerId, BookingStatus.WAITING, getPagination(from, size)));
                    break;
                case REJECTED:
                    bookings = bookingMapper.toDto(repository.findByBookerIdAndStatusIs(
                            bookerId, BookingStatus.REJECTED, getPagination(from, size)));
            }
        }
        log.info("Получен список бронирований {} для пользователя {}", bookings, userService.getUserById(bookerId));
        return bookings;
    }

    @Override
    public List<BookingDto> getByOwnerAndState(Long ownerId, String state, Integer from, Integer size) {
        checkEnum(state);
        List<BookingDto> bookings = new ArrayList<>();
        if (itemService.getItemsByOwner(ownerId, 0, size).size() > 0 &&
                userService.getUserById(ownerId) != null) {
            switch (BookingState.valueOf(state)) {
                case ALL:
                    bookings = bookingMapper.toDto(repository.findByItemOwnerId(ownerId, getPagination(from, size)));
                    break;
                case CURRENT:
                    bookings = bookingMapper.toDto(repository.findByItemOwnerIdAndStartBeforeAndEndAfter(
                            ownerId, LocalDateTime.now(), LocalDateTime.now(), getPagination(from, size)));
                    break;
                case PAST:
                    bookings = bookingMapper.toDto(repository.findByItemOwnerIdAndEndIsBefore(
                            ownerId, LocalDateTime.now(), getPagination(from, size)));
                    break;
                case FUTURE:
                    bookings = bookingMapper.toDto(repository.findByItemOwnerIdAndStartIsAfter(
                            ownerId, LocalDateTime.now(), getPagination(from, size)));
                    break;
                case WAITING:
                    bookings = bookingMapper.toDto(repository.findByItemOwnerIdAndStatusIs(
                            ownerId, BookingStatus.WAITING, getPagination(from, size)));
                    break;
                case REJECTED:
                    bookings = bookingMapper.toDto(repository.findByItemOwnerIdAndStatusIs(
                            ownerId, BookingStatus.REJECTED, getPagination(from, size)));
            }
        }
        log.info("Получен список бронирований {} для владельца {}", bookings, userService.getUserById(ownerId));
        return bookings;
    }

    @Override
    public BookingDto getLastBooking(Long itemId) {
        Booking booking = repository.findFirstByItemIdAndEndIsBefore(itemId, LocalDateTime.now(), orderByDesc());
        return booking == null ? null : bookingMapper.toDto(booking);
    }

    @Override
    public BookingDto getNextBooking(Long itemId) {
        Booking booking = repository.findFirstByItemIdAndStartIsAfter(itemId, LocalDateTime.now(),
                Sort.by(Sort.Direction.ASC, "start"));
        return booking == null ? null : bookingMapper.toDto(booking);
    }

    @Override
    public Booking getByItemId(Long itemId, Long userId, LocalDateTime time) {
        return repository.findByItemIdAndBookerIdAndEndIsBefore(itemId, userId, time);
    }

    private void checkEnum(String string) {
        for (BookingState state : BookingState.values()) {
            if (state.toString().equals(string)) {
                return;
            }
        }
        throw new ValidationException("Unknown state: " + string);
    }

    private Booking checkBooking(Long bookingId) {
        Optional<Booking> booking = repository.findById(bookingId);
        if (booking.isEmpty()) {
            throw new ObjectNotFoundException("Бронирование с id " + bookingId + " не найдено");
        }
        return booking.get();
    }

    private Pageable getPagination(Integer from, Integer size) {
        return new OffsetPageRequest(from, size, orderByDesc());
    }

    private Sort orderByDesc() {
        return Sort.by(Sort.Direction.DESC, "end");
    }
}