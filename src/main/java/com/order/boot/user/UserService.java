package com.order.boot.user;

import com.order.boot.user.dto.CreateUserRequest;
import com.order.boot.user.dto.UpdateUserRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(CONFLICT, "Username already exists");
        }

        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password())
        );

        return userRepository.save(user);
    }

    public User updateUser(Long id, UpdateUserRequest request) {
        User user = findUserById(id);
        Optional<User> existingUser = userRepository.findByUsername(request.username());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
            throw new ResponseStatusException(CONFLICT, "Username already exists");
        }

        user.updateUsername(request.username());
        user.updatePassword(passwordEncoder.encode(request.password()));

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = findUserById(id);
        userRepository.delete(user);
    }
}
