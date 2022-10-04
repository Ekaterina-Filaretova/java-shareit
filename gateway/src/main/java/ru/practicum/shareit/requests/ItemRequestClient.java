package ru.practicum.shareit.requests;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.requests.dto.ItemRequestDto;

import java.util.List;

@Component
public class ItemRequestClient {

    private final RestTemplate template;

    public ItemRequestClient(@Value("${shareit-server.url}/requests") String url, RestTemplateBuilder template) {
        this.template = template
                .errorHandler(new ErrorHandler())
                .uriTemplateHandler(new DefaultUriBuilderFactory(url))
                .build();
    }

    public ResponseEntity<ItemRequestDto> add(Long requesterId, ItemRequestDto requestDto) {
        return template.postForEntity("",
                getHttpEntity(requesterId, requestDto),
                ItemRequestDto.class);
    }

    public ResponseEntity<List<ItemRequestDto>> getAllByRequester(Long requesterId) {
        return template.exchange("",
                HttpMethod.GET,
                getHttpEntity(requesterId, null),
                new ParameterizedTypeReference<>() {
                });
    }

    public ResponseEntity<List<ItemRequestDto>> getAllExceptRequester(Long requesterId, Integer from, Integer size) {
        return template.exchange("/all?from={from}&size={size}",
                HttpMethod.GET,
                getHttpEntity(requesterId, null),
                new ParameterizedTypeReference<>() {
                },
                from, size);
    }

    public ResponseEntity<ItemRequestDto> getById(Long userId, Long requestId) {
        return template.exchange("/{requestId}",
                HttpMethod.GET,
                getHttpEntity(userId, null),
                ItemRequestDto.class,
                requestId);
    }

    private HttpEntity<ItemRequestDto> getHttpEntity(Long requesterId, ItemRequestDto requestDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", String.valueOf(requesterId));
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return requestDto == null ? new HttpEntity<>(headers) : new HttpEntity<>(requestDto, headers);
    }
}