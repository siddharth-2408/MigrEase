package com.migrease.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true)
    private String bookingId;

    @ManyToOne
    private User user;

    private String pickupAddress;
    private String dropAddress;

    private double totalWeight;
    private double distanceInKm;

    //Cost;
    private double transportCost;
    private double packingCost;
    private double serviceCost = 200.00;
    private double totalCost;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<FurnitureItem> furnitureItems = new ArrayList<>();

    private LocalDateTime createdAt;

    private String status = "pending";

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType = null;
}
