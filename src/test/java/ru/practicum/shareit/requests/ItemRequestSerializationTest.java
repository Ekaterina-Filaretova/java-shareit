package ru.practicum.shareit.requests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.requests.dto.ItemRequestDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestSerializationTest {

    @Autowired
    private JacksonTester<ItemRequestDto> tester;

    @Test
    public void serializationTest() throws IOException {
        ItemRequestDto requestDto = new ItemRequestDto(1L, "text",
                getLTD(2), List.of());
        JsonContent<ItemRequestDto> json = tester.write(requestDto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(requestDto.getId().intValue());
        assertThat(json).extractingJsonPathStringValue("$.description").isEqualTo(requestDto.getDescription());
        assertThat(json).extractingJsonPathStringValue("$.created").isEqualTo(requestDto.getCreated().toString());
        assertThat(json).extractingJsonPathArrayValue("$.items").isNotNull();
    }

    @Test
    public void deserializationTest() throws IOException {
        String json = "{\"id\":1,\"description\":\"text\"," +
                "\"created\":\"" + getLTD(2) + "\",\"items\":[]}";
        ItemRequestDto requestDto = tester.parseObject(json);
        assertThat(requestDto.getId()).isEqualTo(1L);
        assertThat(requestDto.getDescription()).isEqualTo("text");
        assertThat(requestDto.getCreated()).isEqualTo(getLTD(2));
        assertThat(requestDto.getItems()).hasSize(0);
    }

    private LocalDateTime getLTD(int sec) {
        return LocalDateTime.now().plusSeconds(sec).truncatedTo(ChronoUnit.SECONDS);
    }
}
