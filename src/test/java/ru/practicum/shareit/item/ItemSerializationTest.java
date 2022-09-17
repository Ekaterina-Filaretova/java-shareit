package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemSerializationTest {

    @Autowired
    private JacksonTester<ItemDto> tester;

    @Test
    public void serializationTest() throws IOException {
        ItemDto itemDto = new ItemDto(1L, "item", "desc", true, 1L);
        JsonContent<ItemDto> json = tester.write(itemDto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(itemDto.getId().intValue());
        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo(itemDto.getName());
        assertThat(json).extractingJsonPathStringValue("$.description").isEqualTo(itemDto.getDescription());
        assertThat(json).extractingJsonPathBooleanValue("$.available").isEqualTo(itemDto.getAvailable());
        assertThat(json).extractingJsonPathNumberValue("$.requestId").isEqualTo(itemDto.getRequestId().intValue());
    }

    @Test
    public void deserializationTest() throws IOException {
        String json = "{\"id\": 1, \"name\": \"item\", \"description\": \"desc\", \"available\": true," +
                "\"requestId\": 1}";
        ItemDto itemDto = tester.parseObject(json);

        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("item");
        assertThat(itemDto.getDescription()).isEqualTo("desc");
        assertThat(itemDto.getAvailable()).isEqualTo(true);
        assertThat(itemDto.getRequestId()).isEqualTo(1L);
    }
}
