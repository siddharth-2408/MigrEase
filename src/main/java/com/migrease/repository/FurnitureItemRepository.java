package com.migrease.repository;

import com.migrease.model.Booking;
import com.migrease.model.FurnitureItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FurnitureItemRepository extends JpaRepository<FurnitureItem, Integer> {
    List<FurnitureItem> findByUserId(Integer id);
    int countByBooking(Booking booking);
}
