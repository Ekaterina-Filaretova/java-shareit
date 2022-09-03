package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

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
        Booking booking = bookingMapper.fromDto(bookingDto);
        booking.setItem(itemService.getItemById(bookingDto.getItemId()));
        if (booking.getItem().getOwner().getId().equals(userId)) {
            throw new ObjectNotFoundException("Владелец не может арендовать собственную вещь");
        }
        booking.setBooker(userMapper.fromDto(userService.getUserById(userId)));
        if (isDateValid(booking) && booking.getItem().getAvailable()) {
            log.info("Добавлено бронирование {}", booking);
            return bookingMapper.toDto(repository.save(booking));
        }
        throw new ValidationException("Предмет " + booking.getItem() + " не доступен");
    }

    @Override
    public BookingDto makeApprove(Long ownerId, Long bookingId, boolean approved) {
        Optional<Booking> booking = repository.findById(bookingId);
        if (booking.isPresent()) {
            if (booking.get().getItem().getOwner().getId().equals(ownerId)) {
                if (booking.get().getStatus().equals(BookingStatus.WAITING)) {
                    if (approved) {
                        booking.get().setStatus(BookingStatus.APPROVED);
                    } else {
                        booking.get().setStatus(BookingStatus.REJECTED);
                    }
                    log.info("Бронированию {} установлен новый статус {}", booking, booking.get().getStatus());
                    return bookingMapper.toDto(repository.save(booking.get()));
                }
                throw new ValidationException("Изменить статус бронирования невозможно");
            }
            throw new ObjectNotFoundException("Изменить статус бронирования может только владелец предмета");
        }
        throw new ObjectNotFoundException("Бронирование с id " + bookingId + " не найдено");
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Optional<Booking> booking = repository.findById(bookingId);
        if (booking.isPresent()) {
            if (booking.get().getBooker().getId().equals(userId)
                    || booking.get().getItem().getOwner().getId().equals(userId)) {
                log.info("Получено бронирование {}", booking.get());
                return bookingMapper.toDto(booking.get());
            }
            throw new ObjectNotFoundException("Просмотреть бронирование может либо владелец вещи, либо автор аренды");
        }
        throw new ObjectNotFoundException("Бронирование с id " + bookingId + " не найдено");
    }

    @Override
    public List<BookingDto> getByUserAndState(Long bookerId, String state) {
        List<BookingDto> bookings = new ArrayList<>();
        if (userService.getUserById(bookerId) != null && checkEnum(state)) {
            switch (BookingState.valueOf(state)) {
                case ALL:
                    bookings = bookingMapper.toDto(repository.findByBookerId(bookerId, orderByDesc()));
                    break;
                case CURRENT:
                    bookings = bookingMapper.toDto(repository.findByBookerIdAndStartIsBeforeAndEndIsAfter(
                            bookerId, LocalDateTime.now(), LocalDateTime.now(), orderByDesc()));
                    break;
                case PAST:
                    bookings = bookingMapper.toDto(repository.findByBookerIdAndEndIsBefore(
                            bookerId, LocalDateTime.now(), orderByDesc()));
                    break;
                case FUTURE:
                    bookings = bookingMapper.toDto(repository.findByBookerIdAndStartIsAfter(
                            bookerId, LocalDateTime.now(), orderByDesc()));
                    break;
                case WAITING:
                    bookings = bookingMapper.toDto(repository.findByBookerIdAndStatusIs(
                            bookerId, BookingStatus.WAITING, orderByDesc()));
                    break;
                case REJECTED:
                    bookings = bookingMapper.toDto(repository.findByBookerIdAndStatusIs(
                            bookerId, BookingStatus.REJECTED, orderByDesc()));
            }
        }
        log.info("Получен список бронирований {} для пользователя {}", bookings, userService.getUserById(bookerId));
        return bookings;
    }

    @Override
    public List<BookingDto> getByOwnerAndState(Long ownerId, String state) {
        List<BookingDto> bookings = new ArrayList<>();
        if (itemService.getItemsByOwner(ownerId).size() > 0 &&
                userService.getUserById(ownerId) != null &&
                checkEnum(state)) {
            switch (BookingState.valueOf(state)) {
                case ALL:
                    bookings = bookingMapper.toDto(repository.findByItemOwnerId(ownerId, orderByDesc()));
                    break;
                case CURRENT:
                    bookings = bookingMapper.toDto(repository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(
                            ownerId, LocalDateTime.now(), LocalDateTime.now(), orderByDesc()));
                    break;
                case PAST:
                    bookings = bookingMapper.toDto(repository.findByItemOwnerIdAndEndIsBefore(
                            ownerId, LocalDateTime.now(), orderByDesc()));
                    break;
                case FUTURE:
                    bookings = bookingMapper.toDto(repository.findByItemOwnerIdAndStartIsAfter(
                            ownerId, LocalDateTime.now(), orderByDesc()));
                    break;
                case WAITING:
                    bookings = bookingMapper.toDto(repository.findByItemOwnerIdAndStatusIs(
                            ownerId, BookingStatus.WAITING, orderByDesc()));
                    break;
                case REJECTED:
                    bookings = bookingMapper.toDto(repository.findByItemOwnerIdAndStatusIs(
                            ownerId, BookingStatus.REJECTED, orderByDesc()));
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

    private boolean isDateValid(Booking booking) {
        if (booking.getEnd().isAfter(booking.getStart())) {
            return true;
        }
        throw new ValidationException("Дата конца аренды не может быть раньше даты начала аренды");
    }

    private Sort orderByDesc() {
        return Sort.by(Sort.Direction.DESC, "end");
    }

    private boolean checkEnum(String string) {
        for (BookingState state : BookingState.values()) {
            if (state.toString().equals(string)) {
                return true;
            }
        }
        throw new ValidationException("Unknown state: " + string);
    }
}
