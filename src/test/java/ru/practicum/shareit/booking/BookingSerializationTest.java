package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingSerializationTest {

    @Autowired
    private JacksonTester<BookingDto> tester;

    @Test
    public void serializationTest() throws IOException {
        BookingDto bookingDto = new BookingDto(1L, getLTD(2), getLTD(4), new Item(), 1L,
                new User(), 1L, BookingStatus.WAITING);
        JsonContent<BookingDto> json = tester.write(bookingDto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(bookingDto.getId().intValue());
        assertThat(json).extractingJsonPathStringValue("$.start").isEqualTo(bookingDto.getStart().toString());
        assertThat(json).extractingJsonPathStringValue("$.end").isEqualTo(bookingDto.getEnd().toString());
        assertThat(json).extractingJsonPathNumberValue("$.itemId").isEqualTo(bookingDto.getItemId().intValue());
        assertThat(json).extractingJsonPathNumberValue("$.bookerId").isEqualTo(bookingDto.getBookerId().intValue());
        assertThat(json).extractingJsonPathStringValue("$.status").isEqualTo(bookingDto.getStatus().toString());
    }

    @Test
    public void deserializationTest() throws IOException {
        String json = "{\"id\":1," +
                "\"start\":\"" + getLTD(2) + "\"," +
                "\"end\":\"" + getLTD(3) + "\"," +
                "\"item\":" +
                "   {\"id\":null,\"name\":null,\"description\":null,\"available\":null," +
                "   \"owner\":null,\"request\":null},\"itemId\":1," +
                "\"booker\":{\"id\":null,\"name\":null,\"email\":null}," +
                "\"bookerId\":1," +
                "\"status\":\"WAITING\"}";
        BookingDto bookingDto = tester.parseObject(json);

        assertThat(bookingDto.getId()).isEqualTo(1L);
        assertThat(bookingDto.getStart()).isEqualTo(getLTD(2));
        assertThat(bookingDto.getEnd()).isEqualTo(getLTD(3));
        assertThat(bookingDto.getItem().toString()).isEqualTo(new Item().toString());
        assertThat(bookingDto.getItemId()).isEqualTo(1L);
        assertThat(bookingDto.getBooker().toString()).isEqualTo(new User().toString());
        assertThat(bookingDto.getBookerId()).isEqualTo(1L);
        assertThat(bookingDto.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    private LocalDateTime getLTD(int sec) {
        return LocalDateTime.now().plusSeconds(sec).truncatedTo(ChronoUnit.SECONDS);
    }
}
