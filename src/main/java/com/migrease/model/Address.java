package com.migrease.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    // Pickup fields
    private String pickupHouse;
    private String pickupStreet;
    private String pickupCity;
    private String pickupPincode;

    // Drop fields
    private String dropHouse;
    private String dropStreet;
    private String dropCity;
    private String dropPincode;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    public String getPickup(){
        return pickupHouse+", "+pickupStreet+", "+pickupCity+", "+pickupPincode;
    }
    public String getDrop(){
        return dropHouse+", "+dropStreet+", "+dropCity+", "+dropPincode;
    }
}
