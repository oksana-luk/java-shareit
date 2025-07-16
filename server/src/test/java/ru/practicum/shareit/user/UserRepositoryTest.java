package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.RandomUtils;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserRepositoryTest {
    private final UserRepository userRepository;

    @Test
    void save_shouldReturnSavedUserWithId() {
        String name = "Some Name";
        String email = "some_email@mail.com";

        User userToSave = new User(0, name, email);

        User savedUser = userRepository.save(userToSave);

        assertNotNull(savedUser);
        assertTrue(savedUser.getId() > 0);
        long savedId = savedUser.getId();

        savedUser = userRepository.findById(savedId).get();
        assertThat(savedUser).usingRecursiveComparison().ignoringFields("id").isEqualTo(userToSave);
    }

    @Test
    void save_shouldReturnUpdatedUserWithId() {
        String name = "Some Name";
        String email = RandomUtils.getRandomEmail();

        User userToSave = new User(0, name, email);

        User savedUser = userRepository.save(userToSave);

        assertNotNull(savedUser);
        assertTrue(savedUser.getId() > 0);
        long savedId = savedUser.getId();

        savedUser = userRepository.findById(savedId).get();
        assertThat(savedUser).usingRecursiveComparison().ignoringFields("id").isEqualTo(userToSave);

        User changedUser = new User(savedUser.getId(), "New name", RandomUtils.getRandomEmail());

        User updatedUser = userRepository.save(changedUser);

        assertThat(updatedUser).usingRecursiveComparison().isEqualTo(changedUser);
    }

    @Test
    void delete_shouldDeleteUserWithId() {
        String name = "Some Name";
        String email = RandomUtils.getRandomEmail();

        User userToSave = new User(0, name, email);

        User savedUser = userRepository.save(userToSave);

        assertNotNull(savedUser);
        assertTrue(savedUser.getId() > 0);
        long savedId = savedUser.getId();

        userRepository.deleteById(savedId);

        Optional<User> deletedUserOpt = userRepository.findById(savedId);
        assertThat(deletedUserOpt).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        String name = "Some Name";
        String email = RandomUtils.getRandomEmail();

        User userToSave = new User(0, name, email);

        User savedUser = userRepository.save(userToSave);

        assertNotNull(savedUser);
        assertTrue(savedUser.getId() > 0);
        long savedId = savedUser.getId();

        Collection<User> users = userRepository.findAll();

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals(savedId, users.stream().toList().getFirst().getId());
    }
}
