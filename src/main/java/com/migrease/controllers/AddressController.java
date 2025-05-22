package com.migrease.controllers;

import com.migrease.model.Address;
import com.migrease.model.Booking;
import com.migrease.model.User;
import com.migrease.repository.AddressRepository;
import com.migrease.repository.BookingRepo;
import com.migrease.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;

@RestController
@RequestMapping("/api/address")
public class AddressController {

    @Autowired
    private AddressRepository addressRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private BookingRepo bookingRepo;

    @PostMapping("/save")
    public ResponseEntity<?> saveAddress(@RequestBody Map<String, String> payload, Principal principal) {
        // Find the user by email
        Optional<User> userOpt = userRepo.findByEmail(principal.getName());
        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(Map.of("error", "User not found"), HttpStatus.NOT_FOUND);
        }

        User user = userOpt.get();

        // Create new address from payload
        Address newAddress = new Address();
        newAddress.setPickupHouse(payload.get("pickupHouse"));
        newAddress.setPickupStreet(payload.get("pickupStreet"));
        newAddress.setPickupCity(payload.get("pickupCity"));
        newAddress.setPickupPincode(payload.get("pickupPincode"));

        newAddress.setDropHouse(payload.get("dropHouse"));
        newAddress.setDropStreet(payload.get("dropStreet"));
        newAddress.setDropCity(payload.get("dropCity"));
        newAddress.setDropPincode(payload.get("dropPincode"));

        newAddress.setUser(user);

        // Check if the same address already exists
        Optional<Address> existingAddressOpt = addressRepo.findByUser(user);
        if(newAddress.getPickup().equals(newAddress.getDrop())) {
            return new ResponseEntity<>(
                    Map.of(
                            "redirect", "/user/booking",
                            "message", "Both address cannot be same, Please be carefull😁"
                    ),
                    HttpStatus.FOUND
            );
        }
        if (existingAddressOpt.isPresent()) {
            Address existingAddress = existingAddressOpt.get();
            // Check if addresses match
            if (addressesMatch(existingAddress, newAddress)) {
                // Check if there's an active booking
                List<Booking> activeBookings = bookingRepo.findByUserAndStatusNotIn(
                        user,
                        List.of("CANCELLED", "COMPLETED")
                );

                if (!activeBookings.isEmpty()) {
                    // There's at least one active booking, redirect to booking page
                    return new ResponseEntity<>(
                            Map.of(
                                    "redirect", "/user/booking",
                                    "message", "You have an active booking. Please wait until it's completed or cancelled."
                            ),
                            HttpStatus.FOUND
                    );
                }
                return new ResponseEntity<>(Map.of("addressId", existingAddress.getId()), HttpStatus.OK);
            }
        }

        // Save the address if it doesn't exist or there's no active booking
        Address saved = addressRepo.save(newAddress);
        return new ResponseEntity<>(Map.of("addressId", saved.getId()), HttpStatus.CREATED);
    }

    /**
     * Compares two addresses to check if they are essentially the same
     */
    private boolean addressesMatch(Address a1, Address a2) {
        return Objects.equals(a1.getPickupHouse(), a2.getPickupHouse()) &&
                Objects.equals(a1.getPickupStreet(), a2.getPickupStreet()) &&
                Objects.equals(a1.getPickupCity(), a2.getPickupCity()) &&
                Objects.equals(a1.getPickupPincode(), a2.getPickupPincode()) &&
                Objects.equals(a1.getDropHouse(), a2.getDropHouse()) &&
                Objects.equals(a1.getDropStreet(), a2.getDropStreet()) &&
                Objects.equals(a1.getDropCity(), a2.getDropCity()) &&
                Objects.equals(a1.getDropPincode(), a2.getDropPincode());
    }
}