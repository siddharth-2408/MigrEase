package com.migrease.repository;

import com.migrease.model.Address;
import com.migrease.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    boolean existsByUser(User user);
    Optional<Address> findByUser(User user);
}
