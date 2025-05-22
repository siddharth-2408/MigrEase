package com.migrease.service;

import com.migrease.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface UserServices {
    User saveUser(User user);

    Optional<User> findByEmail(String email);

    User updateUser(User user);

    void deleteUser(int id);

    boolean existsUserByEmail(String email);

    List<User> getAllUsers();
}
