package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private ItemService itemService;

    @Mock
    private UserService userService;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private BookingRepository repository;

    @Test
    public void addBooking() {
        Item item = new Item(1L, "item", "desc", true, new User(), null);
        item.getOwner().setId(1L);
        User booker = new User(2L, "user", "desc");
        BookingDto bookingDto = new BookingDto(1L, getLTD(2), getLTD(3),
                item, item.getId(), booker, booker.getId(), BookingStatus.WAITING);
        Booking booking = new Booking(1L, bookingDto.getStart(), bookingDto.getEnd(),
                null, null, BookingStatus.WAITING);
        when(bookingMapper.fromDto(bookingDto)).thenReturn(booking);
        when(itemService.getItemById(any())).thenReturn(item);
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(userMapper.fromDto(any())).thenReturn(booker);
        when(repository.save(any())).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        BookingDto savedBooking = bookingService.add(2L, bookingDto);
        assertThat(savedBooking).usingRecursiveComparison().isEqualTo(bookingDto);
    }

    @Test
    public void addBookingWithoutItemExist() {
        BookingDto bookingDto = new BookingDto(1L, getLTD(2), getLTD(3),
                null, 1L, null, null, null);
        Booking booking = new Booking(1L, bookingDto.getStart(), bookingDto.getEnd(),
                null, null, BookingStatus.WAITING);
        when(bookingMapper.fromDto(bookingDto)).thenReturn(booking);
        when(itemService.getItemById(1L)).thenThrow(ObjectNotFoundException.class);

        assertThatThrownBy(() ->
                bookingService.add(1L, bookingDto))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void addBookingWhenUserIsOwner() {
        BookingDto bookingDto = new BookingDto(1L, getLTD(2), getLTD(3),
                null, 1L, null, null, null);
        Booking booking = new Booking(1L, bookingDto.getStart(), bookingDto.getEnd(),
                new Item(), null, BookingStatus.WAITING);
        Item item = new Item(1L, "item", "desc", true, new User(), null);
        item.getOwner().setId(1L);
        when(bookingMapper.fromDto(bookingDto)).thenReturn(booking);
        when(itemService.getItemById(anyLong())).thenReturn(item);

        assertThatThrownBy(() ->
                bookingService.add(1L, bookingDto))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void addBookingWithUnavailableItem() {
        BookingDto bookingDto = new BookingDto(1L, getLTD(2), getLTD(3),
                null, 1L, null, null, null);
        Booking booking = new Booking(1L, bookingDto.getStart(), bookingDto.getEnd(),
                new Item(), null, BookingStatus.WAITING);
        Item item = new Item(1L, "item", "desc", false, new User(), null);
        item.getOwner().setId(1L);
        when(bookingMapper.fromDto(bookingDto)).thenReturn(booking);
        when(itemService.getItemById(anyLong())).thenReturn(item);

        assertThatThrownBy(() ->
                bookingService.add(2L, bookingDto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void makeApprove() {
        Item item = new Item(1L, "item", "desc", true, new User(), null);
        item.getOwner().setId(1L);
        User booker = new User(2L, "user", "desc");
        Booking booking = new Booking(1L, getLTD(2), getLTD(3),
                item, booker, BookingStatus.WAITING);
        BookingDto bookingDto = new BookingDto(1L, getLTD(2), getLTD(3),
                item, item.getId(), booker, booker.getId(), BookingStatus.APPROVED);
        when(repository.findById(any())).thenReturn(Optional.of(booking));
        when(repository.save(any())).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        BookingDto savedBooking = bookingService.makeApprove(1L, 1L, true);
        assertThat(savedBooking).usingRecursiveComparison().isEqualTo(bookingDto);
    }

    @Test
    public void makeReject() {
        Item item = new Item(1L, "item", "desc", true, new User(), null);
        item.getOwner().setId(1L);
        User booker = new User(2L, "user", "desc");
        Booking booking = new Booking(1L, getLTD(2), getLTD(3),
                item, booker, BookingStatus.WAITING);
        BookingDto bookingDto = new BookingDto(1L, getLTD(2), getLTD(3),
                item, item.getId(), booker, booker.getId(), BookingStatus.REJECTED);
        when(repository.findById(any())).thenReturn(Optional.of(booking));
        when(repository.save(any())).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        BookingDto savedBooking = bookingService.makeApprove(1L, 1L, false);
        assertThat(savedBooking).usingRecursiveComparison().isEqualTo(bookingDto);
    }

    @Test
    public void makeApproveWithoutBooking() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                bookingService.makeApprove(1L, 1L, true))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void makeApproveByWrongOwner() {
        Item item = new Item(1L, "item", "desc", true, new User(), null);
        item.getOwner().setId(1L);
        Booking booking = new Booking(1L, getLTD(2), getLTD(3),
                item, new User(), BookingStatus.WAITING);
        when(repository.findById(any())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() ->
                bookingService.makeApprove(2L, 1L, true))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void makeApproveForWrongStatus() {
        Item item = new Item(1L, "item", "desc", true, new User(), null);
        item.getOwner().setId(1L);
        Booking booking = new Booking(1L, getLTD(2), getLTD(3),
                item, new User(), BookingStatus.APPROVED);
        when(repository.findById(any())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() ->
                bookingService.makeApprove(1L, 1L, true))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void getByIdByItemOwner() {
        Item item = new Item(1L, "item", "desc", true, new User(), null);
        item.getOwner().setId(2L);
        User booker = new User(3L, "user", "desc");
        Booking booking = new Booking(4L, getLTD(2), getLTD(3),
                item, booker, BookingStatus.WAITING);
        BookingDto bookingDto = new BookingDto(4L, getLTD(2), getLTD(3),
                item, item.getId(), booker, booker.getId(), BookingStatus.WAITING);
        when(repository.findById(any())).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        BookingDto savedBooking = bookingService.getById(item.getOwner().getId(), booking.getId());
        assertThat(savedBooking).usingRecursiveComparison().isEqualTo(bookingDto);
    }

    @Test
    public void getByIdByBooker() {
        Item item = new Item(1L, "item", "desc", true, new User(), null);
        item.getOwner().setId(2L);
        User booker = new User(3L, "user", "desc");
        Booking booking = new Booking(4L, getLTD(2), getLTD(3),
                item, booker, BookingStatus.WAITING);
        BookingDto bookingDto = new BookingDto(4L, getLTD(2), getLTD(3),
                item, item.getId(), booker, booker.getId(), BookingStatus.WAITING);
        when(repository.findById(any())).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        BookingDto savedBooking = bookingService.getById(booking.getBooker().getId(), booking.getId());
        assertThat(savedBooking).usingRecursiveComparison().isEqualTo(bookingDto);
    }

    @Test
    public void getByIdByWrongUser() {
        Item item = new Item(1L, "item", "desc", true, new User(), null);
        item.getOwner().setId(2L);
        User booker = new User(3L, "user", "desc");
        Booking booking = new Booking(4L, getLTD(2), getLTD(3),
                item, booker, BookingStatus.WAITING);
        when(repository.findById(any())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() ->
                bookingService.getById(1L, booking.getId()))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getByUserAndStateAll() {
        BookingDto bookingDto = new BookingDto(1L, getLTD(2), getLTD(3),
                new Item(), 1L, new User(), 1L, BookingStatus.WAITING);
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(repository.findByBookerId(any(), any())).thenReturn(List.of());
        when(bookingMapper.toDto(anyList())).thenReturn(List.of(bookingDto));

        List<BookingDto> bookings = bookingService.getByUserAndState(1L, "ALL", 0, 1);
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0)).usingRecursiveComparison().isEqualTo(bookingDto);
    }

    @Test
    public void getByUserAndStateCurrent() {
        BookingDto bookingDto = new BookingDto(1L, getLTD(2), getLTD(3),
                new Item(), 1L, new User(), 1L, BookingStatus.WAITING);
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(repository.findByBookerIdAndStartIsBeforeAndEndIsAfter(any(), any(), any(), any())).thenReturn(List.of());
        when(bookingMapper.toDto(anyList())).thenReturn(List.of(bookingDto));

        List<BookingDto> bookings = bookingService.getByUserAndState(1L, "CURRENT", 0, 1);
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0)).usingRecursiveComparison().isEqualTo(bookingDto);
    }

    @Test
    public void getByUserAndStatePast() {
        BookingDto bookingDto = new BookingDto(1L, getLTD(2), getLTD(3),
                new Item(), 1L, new User(), 1L, BookingStatus.WAITING);
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(repository.findByBookerIdAndEndIsBefore(any(), any(), any())).thenReturn(List.of());
        when(bookingMapper.toDto(anyList())).thenReturn(List.of(bookingDto));

        List<BookingDto> bookings = bookingService.getByUserAndState(1L, "PAST", 0, 1);
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0)).usingRecursiveComparison().isEqualTo(bookingDto);
    }

    @Test
    public void getByUserAndStateFuture() {
        BookingDto bookingDto = new BookingDto(1L, getLTD(2), getLTD(3),
                new Item(), 1L, new User(), 1L, BookingStatus.WAITING);
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(repository.findByBookerIdAndStartIsAfter(any(), any(), any())).thenReturn(List.of());
        when(bookingMapper.toDto(anyList())).thenReturn(List.of(bookingDto));

        List<BookingDto> bookings = bookingService.getByUserAndState(1L, "FUTURE", 0, 1);
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0)).usingRecursiveComparison().isEqualTo(bookingDto);
    }

    @Test
    public void getByUserAndStateWaiting() {
        BookingDto bookingDto = new BookingDto(1L, getLTD(2), getLTD(3),
                new Item(), 1L, new User(), 1L, BookingStatus.WAITING);
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(repository.findByBookerIdAndStatusIs(any(), any(), any())).thenReturn(List.of());
        when(bookingMapper.toDto(anyList())).thenReturn(List.of(bookingDto));

        List<BookingDto> bookings = bookingService.getByUserAndState(1L, "WAITING", 0, 1);
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0)).usingRecursiveComparison().isEqualTo(bookingDto);
    }

    @Test
    public void getByUserAndStateRejected() {
        BookingDto bookingDto = new BookingDto(1L, getLTD(2), getLTD(3),
                new Item(), 1L, new User(), 1L, BookingStatus.WAITING);
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(repository.findByBookerIdAndStatusIs(any(), any(), any())).thenReturn(List.of());
        when(bookingMapper.toDto(anyList())).thenReturn(List.of(bookingDto));

        List<BookingDto> bookings = bookingService.getByUserAndState(1L, "REJECTED", 0, 1);
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0)).usingRecursiveComparison().isEqualTo(bookingDto);
    }

    @Test
    public void getByWrongUserAndStateAll() {
        when(userService.getUserById(any())).thenThrow(ObjectNotFoundException.class);

        assertThatThrownBy(() ->
                bookingService.getByUserAndState(1L, "ALL", 0, 1))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getByUserAndWrongState() {
        assertThatThrownBy(() ->
                bookingService.getByUserAndState(1L, "qwe", 0, 1))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void getByOwnerAndStateAll() {
        BookingDto bookingDto = new BookingDto(1L, getLTD(2), getLTD(3),
                new Item(), 1L, new User(), 1L, BookingStatus.WAITING);
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(itemService.getItemsByOwner(any(), any(), any())).thenReturn(List.of(new ItemWithBookingDto()));
        when(repository.findByItemOwnerId(any(), any())).thenReturn(List.of());
        when(bookingMapper.toDto(anyList())).thenReturn(List.of(bookingDto));

        List<BookingDto> bookings = bookingService.getByOwnerAndState(1L, "ALL", 0, 1);
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0)).usingRecursiveComparison().isEqualTo(bookingDto);
    }

    @Test
    public void getByOwnerAndStateCurrent() {
        BookingDto bookingDto = new BookingDto(1L, getLTD(2), getLTD(3),
                new Item(), 1L, new User(), 1L, BookingStatus.WAITING);
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(itemService.getItemsByOwner(any(), any(), any())).thenReturn(List.of(new ItemWithBookingDto()));
        when(repository.findByItemOwnerIdAndStartBeforeAndEndAfter(any(), any(), any(), any())).thenReturn(List.of());
        when(bookingMapper.toDto(anyList())).thenReturn(List.of(bookingDto));

        List<BookingDto> bookings = bookingService.getByOwnerAndState(1L, "CURRENT", 0, 1);
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0)).usingRecursiveComparison().isEqualTo(bookingDto);
    }

    @Test
    public void getByOwnerAndStatePast() {
        BookingDto bookingDto = new BookingDto(1L, getLTD(2), getLTD(3),
                new Item(), 1L, new User(), 1L, BookingStatus.WAITING);
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(itemService.getItemsByOwner(any(), any(), any())).thenReturn(List.of(new ItemWithBookingDto()));
        when(repository.findByItemOwnerIdAndEndIsBefore(any(), any(), any())).thenReturn(List.of());
        when(bookingMapper.toDto(anyList())).thenReturn(List.of(bookingDto));

        List<BookingDto> bookings = bookingService.getByOwnerAndState(1L, "PAST", 0, 1);
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0)).usingRecursiveComparison().isEqualTo(bookingDto);
    }

    @Test
    public void getByOwnerAndStateFuture() {
        BookingDto bookingDto = new BookingDto(1L, getLTD(2), getLTD(3),
                new Item(), 1L, new User(), 1L, BookingStatus.WAITING);
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(itemService.getItemsByOwner(any(), any(), any())).thenReturn(List.of(new ItemWithBookingDto()));
        when(repository.findByItemOwnerIdAndStartIsAfter(any(), any(), any())).thenReturn(List.of());
        when(bookingMapper.toDto(anyList())).thenReturn(List.of(bookingDto));

        List<BookingDto> bookings = bookingService.getByOwnerAndState(1L, "FUTURE", 0, 1);
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0)).usingRecursiveComparison().isEqualTo(bookingDto);
    }

    @Test
    public void getByOwnerAndStateWaiting() {
        BookingDto bookingDto = new BookingDto(1L, getLTD(2), getLTD(3),
                new Item(), 1L, new User(), 1L, BookingStatus.WAITING);
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(itemService.getItemsByOwner(any(), any(), any())).thenReturn(List.of(new ItemWithBookingDto()));
        when(repository.findByItemOwnerIdAndStatusIs(any(), any(), any())).thenReturn(List.of());
        when(bookingMapper.toDto(anyList())).thenReturn(List.of(bookingDto));

        List<BookingDto> bookings = bookingService.getByOwnerAndState(1L, "WAITING", 0, 1);
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0)).usingRecursiveComparison().isEqualTo(bookingDto);
    }

    @Test
    public void getByOwnerAndStateRejected() {
        BookingDto bookingDto = new BookingDto(1L, getLTD(2), getLTD(3),
                new Item(), 1L, new User(), 1L, BookingStatus.WAITING);
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(itemService.getItemsByOwner(any(), any(), any())).thenReturn(List.of(new ItemWithBookingDto()));
        when(repository.findByItemOwnerIdAndStatusIs(any(), any(), any())).thenReturn(List.of());
        when(bookingMapper.toDto(anyList())).thenReturn(List.of(bookingDto));

        List<BookingDto> bookings = bookingService.getByOwnerAndState(1L, "REJECTED", 0, 1);
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0)).usingRecursiveComparison().isEqualTo(bookingDto);
    }

    @Test
    public void getByWrongOwnerAndStateAll() {
        when(itemService.getItemsByOwner(any(), any(), any())).thenReturn(List.of(new ItemWithBookingDto()));
        when(userService.getUserById(any())).thenThrow(ObjectNotFoundException.class);

        assertThatThrownBy(() ->
                bookingService.getByOwnerAndState(1L, "ALL", 0, 1))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getByOwnerAndWrongState() {
        assertThatThrownBy(() ->
                bookingService.getByOwnerAndState(1L, "qwe", 0, 1))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void getByOwnerAndStateWithOwnerWithoutItems() {
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(itemService.getItemsByOwner(any(), any(), any())).thenReturn(List.of());

        List<BookingDto> bookings = bookingService.getByOwnerAndState(1L, "ALL", 0, 1);
        assertThat(bookings).hasSize(0);
    }

    @Test
    public void getLastBooking() {
        BookingDto bookingDto = new BookingDto();
        Booking booking = new Booking();
        when(repository.findFirstByItemIdAndEndIsBefore(any(), any(), any())).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        BookingDto foundBooking = bookingService.getLastBooking(1L);
        assertThat(foundBooking).isNotNull();
    }

    @Test
    public void getLastBookingWithNull() {
        when(repository.findFirstByItemIdAndEndIsBefore(any(), any(), any())).thenReturn(null);

        BookingDto foundBooking = bookingService.getLastBooking(1L);
        assertThat(foundBooking).isNull();
    }

    @Test
    public void getNextBooking() {
        BookingDto bookingDto = new BookingDto();
        Booking booking = new Booking();
        when(repository.findFirstByItemIdAndStartIsAfter(any(), any(), any())).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        BookingDto foundBooking = bookingService.getNextBooking(1L);
        assertThat(foundBooking).isNotNull();
    }

    @Test
    public void getNextBookingWithNull() {
        when(repository.findFirstByItemIdAndStartIsAfter(any(), any(), any())).thenReturn(null);

        BookingDto foundBooking = bookingService.getNextBooking(1L);
        assertThat(foundBooking).isNull();
    }

    private LocalDateTime getLTD(int sec) {
        return LocalDateTime.now().plusSeconds(sec).truncatedTo(ChronoUnit.SECONDS);
    }
}
