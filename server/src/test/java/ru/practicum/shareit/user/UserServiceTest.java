package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.RandomUtils;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock private UserRepository userRepositoryMock;

    @Mock
    private UserMapper userMapperMock;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepositoryMock);
    }

    @Test
    void findUserById_shouldReturnUser() {
        String name = "Some Name";
        String email = RandomUtils.getRandomEmail();
        long id = 12L;

        User userToSave = new User(id, name, email);

        when(userRepositoryMock.findById(id)).thenReturn(Optional.of(userToSave));

        UserDto findedUser = userService.findUserById(id);

        assertNotNull(findedUser);
        assertEquals(id, findedUser.getId());
        assertEquals(name, findedUser.getName());
        assertEquals(email, findedUser.getEmail());

        verify(userRepositoryMock).findById(id);
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        String name = "Some Name";
        String email = "some_email@mail.com";
        long id = 12L;

        Collection<UserDto> expectedUsers = List.of(UserDto.builder().id(id).name(name).email(email).build());

        when(userRepositoryMock.findAll())
                .thenReturn(List.of(new User(id, name, email)));
        Collection<UserDto> actualUsers = userService.getAllUsers();

        assertNotNull(actualUsers);
        assertThat(actualUsers).containsExactlyElementsOf(expectedUsers);

        verify(userRepositoryMock).findAll();
    }

    @Test
    void saveUser_shouldReturnNewUser() {
        String name = "Some Name";
        String email = RandomUtils.getRandomEmail();
        long id = 12L;

        NewUserRequest newUserRequest = NewUserRequest.builder().name(name).email(email).build();
        User userToSave = new User(0L, name, email);
        User savedUser = new User(id, name, email);

        when(userRepositoryMock.save(userToSave)).thenReturn(savedUser);

        UserDto actualUser = userService.saveUser(newUserRequest);

        assertNotNull(actualUser);
        assertEquals(id, actualUser.getId());
        assertEquals(name, actualUser.getName());
        assertEquals(email, actualUser.getEmail());

        verify(userRepositoryMock).save(userToSave);
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() {
        String name = "Some Name";
        String email = RandomUtils.getRandomEmail();
        String newName = name + "upd";
        String newEmail = RandomUtils.getRandomEmail();
        long id = 12L;

        UpdateUserRequest updateUserRequest = new UpdateUserRequest(newName, newEmail);
        User userToUpdate = new User(id, name, email);

        when(userRepositoryMock.findById(id)).thenReturn(Optional.of(userToUpdate));

        when(userRepositoryMock.save(userToUpdate)).thenReturn(userToUpdate);

        UserDto updatedUser = userService.updateUser(updateUserRequest, id);

        assertNotNull(updatedUser);
        assertEquals(id, updatedUser.getId());
        assertEquals(newName, updatedUser.getName());
        assertEquals(newEmail, updatedUser.getEmail());

        verify(userRepositoryMock).findById(id);
        verify(userRepositoryMock).save(userToUpdate);
    }

    @Test
    void deleteUserById_shouldNotReturn() {
        String name = "Some Name";
        String email = RandomUtils.getRandomEmail();
        long id = 12L;

        User user = new User(id, name, email);

        when(userRepositoryMock.findById(id)).thenReturn(Optional.of(user));
        doNothing().when(userRepositoryMock).delete(user);

        assertDoesNotThrow(() -> userService.deleteUserById(id));

        verify(userRepositoryMock).delete(user);
        verify(userRepositoryMock).findById(id);
    }


    @Test
    void findUserById_shouldThrowExceptionWhenNotFound() {
        Long id = 12L;

        when(userRepositoryMock.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.findUserById(id));

        verify(userRepositoryMock).findById(id);
    }

    @Test
    void deleteUser_shouldThrowExceptionWhenNotFound() {
        String name = "Some Name";
        String email = RandomUtils.getRandomEmail();
        long id = 12L;

        User user = new User(id, name, email);

        when(userRepositoryMock.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.deleteUserById(id));

        verify(userRepositoryMock, never()).delete(user);
        verify(userRepositoryMock).findById(id);
    }

    @Test
    void updateUser_shouldThrowExceptionWhenNotFound() {
        String name = "Some Name";
        String email = RandomUtils.getRandomEmail();
        String newName = name + "upd";
        String newEmail = RandomUtils.getRandomEmail();
        long id = 12L;

        UpdateUserRequest userToUpdate = new UpdateUserRequest(newName, newEmail);
        User toUpdate = new User(id, newName, newEmail);

        when(userRepositoryMock.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(userToUpdate, id));

        verify(userRepositoryMock).findById(id);
        verify(userRepositoryMock, never()).save(toUpdate);
    }
}
