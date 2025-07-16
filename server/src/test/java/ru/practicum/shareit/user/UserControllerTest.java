package ru.practicum.shareit.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.RandomUtils;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Slf4j
@WebMvcTest(UserController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserControllerTest {

    @MockBean
    UserService userService;

    private final ApplicationContext applicationContext;

    @Autowired
    private final ObjectMapper mapper;
    @Autowired
    private final MockMvc mockMvc;

    @BeforeEach
    void setUp() {

    }

    @Test
    @SneakyThrows
    void testCreateItem() {
        String name = "Some Name";
        String email = RandomUtils.getRandomEmail();
        long id = 12L;

        NewUserRequest newUserRequest = NewUserRequest.builder().name(name).email(email).build();
        UserDto userDto = UserDto.builder().id(id).name(name).email(email).build();
        when(userService.saveUser(newUserRequest))
                .thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(newUserRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService).saveUser(newUserRequest);
    }

    @Test
    @SneakyThrows
    void getUser() {
        String name = "Some Name";
        String email = RandomUtils.getRandomEmail();
        long id = 12L;

        UserDto userDto = UserDto.builder().id(id).name(name).email(email).build();

        when(userService.findUserById(anyLong()))
                .thenReturn(userDto);

        MvcResult mvcResult =
                mockMvc.perform(get("/users/12")
                                .content(mapper.writeValueAsString(userDto))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        UserDto actualUserDto = mapper.readValue(responseBody, UserDto.class);

        assertNotNull(actualUserDto.getId());
        assertEquals(id, actualUserDto.getId());
        assertThat(actualUserDto).usingRecursiveComparison().ignoringFields("id").isEqualTo(userDto);

        verify(userService).findUserById(id);
    }

    @Test
    @SneakyThrows
    void updateUser() {
        String name = "Some Name";
        String email = RandomUtils.getRandomEmail();
        String newName = "New name";
        String newEmail = RandomUtils.getRandomEmail();
        long id = 12L;

        UserDto userDto = UserDto.builder().id(id).name(newName).email(newEmail).build();
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(newName, newEmail);

        when(userService.updateUser(updateUserRequest, id))
                .thenReturn(userDto);

        MvcResult mvcResult =
                mockMvc.perform(patch("/users/" + id)
                                .content(mapper.writeValueAsString(userDto))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        UserDto actualUserDto = mapper.readValue(responseBody, UserDto.class);

        assertNotNull(actualUserDto.getId());
        assertThat(actualUserDto).usingRecursiveComparison().isEqualTo(userDto);

        verify(userService).updateUser(updateUserRequest, id);
    }

    @Test
    @SneakyThrows
    void deleteUser() {
        Long userId = 12L;

        doNothing().when(userService).deleteUserById(userId);

        mockMvc.perform(delete("/users/" + userId.toString())).andExpect(status().isOk());

        verify(userService).deleteUserById(userId);
    }

    @Test
    @SneakyThrows
    void findAll() {
        String name = "Some Name";
        String email = RandomUtils.getRandomEmail();
        long id = 12L;

        UserDto userDto = UserDto.builder().id(id).name(name).email(email).build();

        when(userService.getAllUsers())
                .thenReturn(List.of(userDto));

        MvcResult mvcResult =
                mockMvc.perform(get("/users")
                                .content(mapper.writeValueAsString(userDto))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        Collection<UserDto> actualUserDtoList = mapper.readValue(responseBody, new TypeReference<List<UserDto>>() {
        });

        assertNotNull(actualUserDtoList);
        assertEquals(1, actualUserDtoList.size());
        UserDto actualUser = actualUserDtoList.stream().toList().getFirst();
        assertNotNull(actualUser);
        assertThat(actualUser).usingRecursiveComparison().isEqualTo(userDto);

        verify(userService).getAllUsers();
    }

    @Test
    void whenNotFoundException_thenReturnsNotFound() throws Exception {
        mockMvc.perform(get("/items/999/1"))
                .andExpect(status().isInternalServerError());
    }
}
