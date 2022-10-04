package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;

import java.util.List;

@Component
public class ItemClient {

    private final RestTemplate template;

    public ItemClient(@Value("${shareit-server.url}/items") String url, RestTemplateBuilder template) {
        this.template = template
                .errorHandler(new ErrorHandler())
                .uriTemplateHandler(new DefaultUriBuilderFactory(url))
                .build();
    }

    public ResponseEntity<ItemDto> addItem(Long userId, ItemDto itemDto) {
        return template.postForEntity("",
                getHttpEntity(userId, itemDto),
                ItemDto.class);
    }

    public ResponseEntity<ItemDto> updateItem(Long itemId, Long userId, ItemDto itemDto) {
        return template.exchange("/{itemId}",
                HttpMethod.PATCH,
                getHttpEntity(userId, itemDto),
                ItemDto.class,
                itemId);
    }

    public ResponseEntity<ItemWithBookingDto> getItemById(Long itemId, Long userId) {
        return template.exchange("/{itemId}",
                HttpMethod.GET,
                getHttpEntity(userId, null),
                ItemWithBookingDto.class,
                itemId);
    }

    public ResponseEntity<List<ItemWithBookingDto>> getItemsByOwner(Long userId, Integer from, Integer size) {
        return template.exchange("?from={from}&size={size}",
                HttpMethod.GET,
                getHttpEntity(userId, null),
                new ParameterizedTypeReference<>() {
                },
                from, size);
    }

    public ResponseEntity<List<ItemDto>> getItemsBySearch(Long userId, String text, Integer from, Integer size) {
        return template.exchange("/search?text={text}&from={from}&size={size}",
                HttpMethod.GET,
                getHttpEntity(userId, null),
                new ParameterizedTypeReference<>() {
                },
                text, from, size);
    }

    public ResponseEntity<CommentDto> addComment(Long itemId, Long userId, CommentDto commentDto) {
        return template.postForEntity("/{itemId}/comment",
                getHttpEntity(userId, commentDto),
                CommentDto.class,
                itemId);
    }

    private <T> HttpEntity<T> getHttpEntity(Long userId, T dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(userId));
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return dto == null ? new HttpEntity<>(headers) : new HttpEntity<>(dto, headers);
    }
}
