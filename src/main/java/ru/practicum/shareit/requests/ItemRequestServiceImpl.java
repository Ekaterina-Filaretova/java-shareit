package ru.practicum.shareit.requests;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.utils.OffsetPageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository repository;
    private final UserService userService;
    private final ItemService itemService;
    private final ItemRequestMapper requestMapper;

    public ItemRequestServiceImpl(ItemRequestRepository repository,
                                  UserService userService,
                                  ItemService itemService,
                                  ItemRequestMapper requestMapper) {
        this.repository = repository;
        this.userService = userService;
        this.itemService = itemService;
        this.requestMapper = requestMapper;
    }

    @Override
    public ItemRequestDto add(Long userId, ItemRequestDto requestDto) {
        checkUser(userId);
        ItemRequest request = requestMapper.convertFromDto(requestDto);
        request.getRequester().setId(userId);
        request.setCreated(LocalDateTime.now());
        log.info("Добавлен запрос вещи {}", request);
        return requestMapper.convertToDto(repository.save(request));
    }

    @Override
    public List<ItemRequestDto> getAllByRequester(Long requesterId) {
        checkUser(requesterId);
        return setItems(repository.findAllByRequesterId(requesterId, getSorting()));
    }

    @Override
    public List<ItemRequestDto> getAllExceptRequester(Long requesterId, Integer from, Integer size) {
        checkUser(requesterId);
        return setItems(repository.findAllByRequesterIdNot(requesterId, getPagination(from, size)));
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        checkUser(userId);
        ItemRequestDto request = requestMapper.convertToDto(checkRequest(requestId));
        request.setItems(itemService.getAllByRequestId(requestId));
        log.info("Получен запрос {}", request);
        return request;
    }

    public List<ItemRequestDto> setItems(List<ItemRequest> requests) {
        List<ItemRequestDto> requestDtos = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            requestDtos.add(requestMapper.convertToDto(requests.get(i)));
            requestDtos.get(i).setItems(itemService.getAllByRequestId(requests.get(i).getId()));
        }
        log.info("Получен список запросов {}", requestDtos);
        return requestDtos;
    }

    private void checkUser(Long userId) {
        if (userService.getUserById(userId) == null) {
            throw new ObjectNotFoundException("Пользователь с id " + userId + " не найден");
        }
    }

    private ItemRequest checkRequest(Long requestId) {
        Optional<ItemRequest> request = repository.findById(requestId);
        if (request.isEmpty()) {
            throw new ObjectNotFoundException("Запрос с id " + requestId + " не найден");
        }
        return request.get();
    }

    private Pageable getPagination(Integer from, Integer size) {
        return new OffsetPageRequest(from, size, getSorting());
    }

    private Sort getSorting() {
        return Sort.by(Sort.Direction.DESC, "created");
    }
}
