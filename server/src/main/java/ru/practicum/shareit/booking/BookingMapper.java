package ru.practicum.shareit.booking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.util.ArrayList;
import java.util.List;

@Component
public class BookingMapper {

    public Booking fromDto(BookingDto bookingDto) {
        return new Booking(bookingDto.getId(),
                bookingDto.getStart(),
                bookingDto.getEnd(),
                new Item(),
                new User(),
                BookingStatus.WAITING);
    }

    public BookingDto toDto(Booking booking) {
        return new BookingDto(booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem(),
                booking.getItem().getId(),
                booking.getBooker(),
                booking.getBooker().getId(),
                booking.getStatus());
    }

    public List<BookingDto> toDto(List<Booking> bookings) {
        List<BookingDto> bookingDtos = new ArrayList<>();
        for (Booking booking : bookings) {
            bookingDtos.add(toDto(booking));
        }
        return bookingDtos;
    }
}
