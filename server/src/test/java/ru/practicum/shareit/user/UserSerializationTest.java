package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserSerializationTest {

    @Autowired
    private JacksonTester<UserDto> tester;

    @Test
    public void serializationTest() throws IOException {
        UserDto userDto = new UserDto(1L, "user", "qwe@mail.com");
        JsonContent<UserDto> json = tester.write(userDto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(userDto.getId().intValue());
        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo(userDto.getName());
        assertThat(json).extractingJsonPathStringValue("$.email").isEqualTo(userDto.getEmail());
    }

    @Test
    public void deserializationTest() throws IOException {
        String json = "{\"id\": 1, \"name\": \"user\", \"email\": \"qwe@mail.com\"}";
        UserDto userDto = tester.parseObject(json);

        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getName()).isEqualTo("user");
        assertThat(userDto.getEmail()).isEqualTo("qwe@mail.com");
    }
}
