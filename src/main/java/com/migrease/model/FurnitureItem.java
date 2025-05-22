package com.migrease.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FurnitureItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String category;
    private double width;
    private double height;
    private double depth;
    private double weight;
    private double quantity;
    private String quality;
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    private Booking booking;

}
