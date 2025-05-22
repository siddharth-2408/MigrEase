package com.migrease.repository;

import com.migrease.model.Booking;
import com.migrease.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepo extends JpaRepository<Booking, Integer> {
    List<Booking> findByUser(User user);
    Optional<Booking> findByBookingId(String bookingId);
    List<Booking> findByUserAndStatusNotIn(User user, List<String> statuses);
}
