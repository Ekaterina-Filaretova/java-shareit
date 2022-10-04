package ru.practicum.shareit.item;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    private final String url = "/items";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemClient client;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void addItem() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "item", "desc", true, null);
        when(client.addItem(any(), any())).thenReturn(new ResponseEntity<>(itemDto, HttpStatus.OK));

        mockMvc.perform(postRequest(itemDto, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId())));
    }

    @Test
    public void addItemWithoutName() throws Exception {
        ItemDto itemDto = new ItemDto(1L, null, "desc", true, null);

        mockMvc.perform(postRequest(itemDto, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addItemWithEmptyName() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "", "desc", true, null);

        mockMvc.perform(postRequest(itemDto, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addItemWithBlankName() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "   ", "desc", true, null);

        mockMvc.perform(postRequest(itemDto, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addItemWithoutDescription() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "item", null, true, null);

        mockMvc.perform(postRequest(itemDto, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addItemWithEmptyDescription() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "item", "", true, null);

        mockMvc.perform(postRequest(itemDto, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addItemWithBlankDescription() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "item", "   ", true, null);

        mockMvc.perform(postRequest(itemDto, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addItemWithoutAvailable() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "item", "desc", null, null);

        mockMvc.perform(postRequest(itemDto, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addItemWithoutHeader() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "item", "desc", null, null);

        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateItem() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "item", "desc", true, null);
        when(client.updateItem(any(), any(), any())).thenReturn(new ResponseEntity<>(itemDto, HttpStatus.OK));

        mockMvc.perform(patchRequest(itemDto, itemDto.getId(), 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId())));
    }

    @Test
    public void updateItemNameOnly() throws Exception {
        ItemDto itemDto = new ItemDto(null, "item", null, null, null);
        ItemDto updatedItem = new ItemDto(1L, "item", "desc", true, null);
        when(client.updateItem(any(), any(), any())).thenReturn(new ResponseEntity<>(updatedItem, HttpStatus.OK));

        mockMvc.perform(patchRequest(itemDto, 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedItem.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updatedItem.getName())))
                .andExpect(jsonPath("$.description", is(updatedItem.getDescription())))
                .andExpect(jsonPath("$.available", is(updatedItem.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(updatedItem.getRequestId())));
    }

    @Test
    public void updateItemDescriptionOnly() throws Exception {
        ItemDto itemDto = new ItemDto(null, null, "desc", null, null);
        ItemDto updatedItem = new ItemDto(1L, "item", "desc", true, null);
        when(client.updateItem(any(), any(), any())).thenReturn(new ResponseEntity<>(updatedItem, HttpStatus.OK));

        mockMvc.perform(patchRequest(itemDto, 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedItem.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updatedItem.getName())))
                .andExpect(jsonPath("$.description", is(updatedItem.getDescription())))
                .andExpect(jsonPath("$.available", is(updatedItem.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(updatedItem.getRequestId())));
    }

    @Test
    public void updateItemAvailableOnly() throws Exception {
        ItemDto itemDto = new ItemDto(null, null, null, true, null);
        ItemDto updatedItem = new ItemDto(1L, "item", "desc", true, null);
        when(client.updateItem(any(), any(), any())).thenReturn(new ResponseEntity<>(updatedItem, HttpStatus.OK));

        mockMvc.perform(patchRequest(itemDto, 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedItem.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updatedItem.getName())))
                .andExpect(jsonPath("$.description", is(updatedItem.getDescription())))
                .andExpect(jsonPath("$.available", is(updatedItem.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(updatedItem.getRequestId())));
    }

    @Test
    public void updateItemWithoutHeader() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "item", "desc", null, null);

        mockMvc.perform(MockMvcRequestBuilders.patch(url + "/" + itemDto.getId())
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getItemById() throws Exception {
        ItemWithBookingDto itemDto = new ItemWithBookingDto(1L, "item", "desc", true,
                null, null, null, null);
        when(client.getItemById(any(), any())).thenReturn(new ResponseEntity<>(itemDto, HttpStatus.OK));

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/" + itemDto.getId())
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    public void getItemByIdWithoutHeader() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url + "/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getItemsByOwner() throws Exception {
        ItemWithBookingDto itemDto = new ItemWithBookingDto(1L, "item", "desc", true,
                null, null, null, null);
        ItemWithBookingDto itemDto2 = new ItemWithBookingDto(2L, "new item", "new desc", false,
                null, null, null, null);
        when(client.getItemsByOwner(any(), any(), any()))
                .thenReturn(new ResponseEntity<>(List.of(itemDto, itemDto2), HttpStatus.OK));

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$[1].id", is(itemDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(itemDto2.getName())))
                .andExpect(jsonPath("$[1].description", is(itemDto2.getDescription())))
                .andExpect(jsonPath("$[1].available", is(itemDto2.getAvailable())));
    }

    @Test
    public void getItemsByOwnerWithoutHeader() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getItemsBySearch() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "item", "desc", true, null);
        ItemDto itemDto2 = new ItemDto(2L, "new item", "new desc", false, null);
        when(client.getItemsBySearch(any(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>(List.of(itemDto, itemDto2), HttpStatus.OK));

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "item"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$[1].id", is(itemDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(itemDto2.getName())))
                .andExpect(jsonPath("$[1].description", is(itemDto2.getDescription())))
                .andExpect(jsonPath("$[1].available", is(itemDto2.getAvailable())));
    }

    @Test
    public void getItemsBySearchWithoutText() throws Exception {
        when(client.getItemsBySearch(any(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>(List.of(), HttpStatus.OK));

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/search")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void addComment() throws Exception {
        CommentDto comment = new CommentDto(1L, "com", null, null);
        when(client.addComment(any(), any(), any())).thenReturn(new ResponseEntity<>(comment, HttpStatus.OK));

        mockMvc.perform(postRequest(comment, 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(comment.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(comment.getText())))
                .andExpect(jsonPath("$.authorName", is(comment.getAuthorName())))
                .andExpect(jsonPath("$.created", is(comment.getCreated())));
    }

    @Test
    public void addCommentWithoutText() throws Exception {
        CommentDto comment = new CommentDto(1L, null, "name", null);
        when(client.addComment(any(), any(), any())).thenReturn(new ResponseEntity<>(comment, HttpStatus.OK));

        mockMvc.perform(postRequest(comment, 1L, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addCommentWithEmptyText() throws Exception {
        CommentDto comment = new CommentDto(1L, "", "name", null);
        when(client.addComment(any(), any(), any())).thenReturn(new ResponseEntity<>(comment, HttpStatus.OK));

        mockMvc.perform(postRequest(comment, 1L, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addCommentWithBlankText() throws Exception {
        CommentDto comment = new CommentDto(1L, "   ", "name", null);
        when(client.addComment(any(), any(), any())).thenReturn(new ResponseEntity<>(comment, HttpStatus.OK));

        mockMvc.perform(postRequest(comment, 1L, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addCommentWithoutHeader() throws Exception {
        CommentDto comment = new CommentDto(1L, null, "name", null);
        when(client.addComment(any(), any(), any())).thenReturn(new ResponseEntity<>(comment, HttpStatus.OK));

        mockMvc.perform(MockMvcRequestBuilders.post(url + "/1/comment")
                        .content(objectMapper.writeValueAsString(comment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getItemByIdWithComments() throws Exception {
        CommentDto comment = new CommentDto(1L, "text", "name", null);
        CommentDto comment2 = new CommentDto(2L, "comment", "new name", null);
        ItemWithBookingDto itemDto = new ItemWithBookingDto(1L, "item", "desc", true,
                null, null, List.of(comment, comment2), null);
        when(client.getItemById(any(), any()))
                .thenReturn(new ResponseEntity<>(itemDto, HttpStatus.OK));

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/" + itemDto.getId())
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.comments", hasSize(2)))
                .andExpect(jsonPath("$.comments[0].id", is(comment.getId()), Long.class))
                .andExpect(jsonPath("$.comments[0].text", is(comment.getText())))
                .andExpect(jsonPath("$.comments[1].id", is(comment2.getId()), Long.class))
                .andExpect(jsonPath("$.comments[1].text", is(comment2.getText())));
    }

    @Test
    public void getItemByOwnerWithComments() throws Exception {
        CommentDto comment = new CommentDto(1L, "text", "name", null);
        CommentDto comment2 = new CommentDto(2L, "comment", "new name", null);
        ItemWithBookingDto itemDto = new ItemWithBookingDto(1L, "item", "desc", true,
                null, null, List.of(comment, comment2), null);
        ItemWithBookingDto itemDto2 = new ItemWithBookingDto(2L, "item", "desc", true,
                null, null, List.of(), null);
        when(client.getItemsByOwner(any(), any(), any()))
                .thenReturn(new ResponseEntity<>(List.of(itemDto, itemDto2), HttpStatus.OK));

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].comments", hasSize(2)))
                .andExpect(jsonPath("$[0].comments[0].id", is(comment.getId()), Long.class))
                .andExpect(jsonPath("$[0].comments[0].text", is(comment.getText())))
                .andExpect(jsonPath("$[0].comments[1].id", is(comment2.getId()), Long.class))
                .andExpect(jsonPath("$[0].comments[1].text", is(comment2.getText())))
                .andExpect(jsonPath("$[1].id", is(itemDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(itemDto2.getName())))
                .andExpect(jsonPath("$[1].comments", hasSize(0)));
    }

    @Test
    public void getItemByIdWithBookings() throws Exception {
        BookingDto lastBooking = new BookingDto(1L, LocalDateTime.now().minusDays(3).truncatedTo(ChronoUnit.SECONDS),
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.SECONDS), new Item(), 1L, new User(), 1L,
                BookingStatus.APPROVED);
        BookingDto nextBooking = new BookingDto(2L, LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS),
                LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.SECONDS), new Item(), 2L, new User(), 1L,
                BookingStatus.WAITING);
        ItemWithBookingDto itemDto = new ItemWithBookingDto(1L, "item", "desc", true,
                lastBooking, nextBooking, List.of(), null);
        when(client.getItemById(any(), any())).thenReturn(new ResponseEntity<>(itemDto, HttpStatus.OK));

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/" + itemDto.getId())
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.lastBooking.id", is(lastBooking.getId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.start", is(lastBooking.getStart().toString())))
                .andExpect(jsonPath("$.lastBooking.end", is(lastBooking.getEnd().toString())))
                .andExpect(jsonPath("$.lastBooking.item", notNullValue()))
                .andExpect(jsonPath("$.lastBooking.itemId", is(lastBooking.getItemId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.booker", notNullValue()))
                .andExpect(jsonPath("$.lastBooking.bookerId", is(lastBooking.getBookerId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.status", is(lastBooking.getStatus().toString())))
                .andExpect(jsonPath("$.nextBooking.id", is(nextBooking.getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.start", is(nextBooking.getStart().toString())))
                .andExpect(jsonPath("$.nextBooking.end", is(nextBooking.getEnd().toString())))
                .andExpect(jsonPath("$.nextBooking.item", notNullValue()))
                .andExpect(jsonPath("$.nextBooking.itemId", is(nextBooking.getItemId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.booker", notNullValue()))
                .andExpect(jsonPath("$.nextBooking.bookerId", is(nextBooking.getBookerId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.status", is(nextBooking.getStatus().toString())));
    }

    @Test
    public void getItemByOwnerWithBookings() throws Exception {
        BookingDto lastBooking = new BookingDto(1L, null, null, new Item(), 1L, new User(), 1L,
                BookingStatus.APPROVED);
        BookingDto nextBooking = new BookingDto(2L, null, null, new Item(), 2L, new User(), 1L,
                BookingStatus.WAITING);
        ItemWithBookingDto itemDto = new ItemWithBookingDto(1L, "item", "desc", true,
                lastBooking, nextBooking, List.of(), null);
        ItemWithBookingDto itemDto2 = new ItemWithBookingDto(2L, "new item", "new desc", true,
                null, null, List.of(), null);
        when(client.getItemsByOwner(any(), any(), any()))
                .thenReturn(new ResponseEntity<>(List.of(itemDto, itemDto2), HttpStatus.OK));

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].lastBooking.id", is(lastBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking.id", is(nextBooking.getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(itemDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(itemDto2.getName())))
                .andExpect(jsonPath("$[1].lastBooking", nullValue()))
                .andExpect(jsonPath("$[1].nextBooking", nullValue()));
    }


    private MockHttpServletRequestBuilder postRequest(ItemDto item, Long ownerId) throws JsonProcessingException {
        return MockMvcRequestBuilders.post(url)
                .header("X-Sharer-User-Id", ownerId)
                .content(objectMapper.writeValueAsString(item))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder patchRequest(ItemDto item,
                                                       Long itemId,
                                                       Long ownerId) throws JsonProcessingException {
        return MockMvcRequestBuilders.patch(url + "/" + itemId)
                .header("X-Sharer-User-Id", ownerId)
                .content(objectMapper.writeValueAsString(item))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder postRequest(CommentDto commentDto,
                                                      Long itemId,
                                                      Long userId) throws JsonProcessingException {
        return MockMvcRequestBuilders.post(url + "/" + itemId + "/comment")
                .header("X-Sharer-User-Id", userId)
                .content(objectMapper.writeValueAsString(commentDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }
}
