package com.migrease.repository;

import com.migrease.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {
    boolean existsUserByEmail(String email);
    Optional<User> findByEmail(String email);
}
