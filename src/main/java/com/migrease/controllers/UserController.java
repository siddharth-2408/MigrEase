package com.migrease.controllers;

import com.migrease.helper.BookingReceiptGenerator;
import com.migrease.helper.DistanceCalculator;
import com.migrease.model.Address;
import com.migrease.model.Booking;
import com.migrease.model.FurnitureItem;
import com.migrease.model.User;
import com.migrease.repository.AddressRepository;
import com.migrease.repository.BookingRepo;
import com.migrease.repository.FurnitureItemRepository;
import com.migrease.repository.UserRepo;
import com.migrease.service.EmailService;
import com.migrease.service.UserServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserServices userServices;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private FurnitureItemRepository furnitureRepo;

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private UserRepo userRepo;

    @GetMapping("/dashboard")
    public String dashboard(@ModelAttribute("loggedUser") User user, Model model) {
        if (user == null) return "redirect:/login";

        boolean hasAddress = addressRepository.existsByUser(user);
        model.addAttribute("hasAddress", hasAddress);

        if (hasAddress) {
            Address address = addressRepository.findByUser(user).orElse(null);
            List<FurnitureItem> items = furnitureRepo.findByUserId(user.getId());

            double totalWeight = items.stream().mapToDouble(FurnitureItem::getWeight).sum();
            double volume = items.stream().mapToDouble(i -> (i.getWidth() * i.getHeight() * i.getDepth()) / 1_000_000.0).sum();

            String truckType = volume < 5 ? "Mini Truck" :
                    volume < 15 ? "Medium Truck" : "Large Truck";

            model.addAttribute("address", address);
            model.addAttribute("totalWeight", totalWeight);
            model.addAttribute("volume", String.format("%.2f", volume));
            model.addAttribute("truckType", truckType);
            model.addAttribute("bookingId", "MOVE" + user.getId()); // Placeholder: use real bookingId if stored
        }

        return "user/dashboard/user-dashboard";
    }


    @GetMapping("/booking")
    public String booking(Model model) {
        return "user/dashboard/booking-dashboard";
    }

    @GetMapping("/confirm-booking")
    public String showConfirmBooking(Model model, Principal principal) {
        User user = userServices.findByEmail(principal.getName()).orElseThrow();
        List<FurnitureItem> items = furnitureRepo.findByUserId(user.getId());
        double totalWeight = items.stream().mapToDouble(FurnitureItem::getWeight).sum();

        model.addAttribute("furnitureItems", items);
        model.addAttribute("totalWeight", totalWeight);

        return "user/booking/confirm-booking";
    }

    @PostMapping("/confirm-booking")
    public String confirmBooking(Principal principal) throws IOException, InterruptedException {
        User user = userRepo.findByEmail(principal.getName()).orElseThrow();
        Address address = addressRepository.findByUser(user).orElseThrow();

        String pickup = address.getPickup();
        String drop = address.getDrop();

        String apiKey = "pk.d62787b8bb1d6592104ca812083c12c8";
        DistanceCalculator calculator = new DistanceCalculator();
        double pickupLat = 0, pickupLon = 0, dropLat = 0, dropLon = 0;

        try {
            double[] pickupCoords = calculator.getLatLng(pickup, apiKey);
            double[] dropCoords = calculator.getLatLng(drop, apiKey);
            pickupLat = pickupCoords[0];
            pickupLon = pickupCoords[1];
            dropLat = dropCoords[0];
            dropLon = dropCoords[1];
        } catch (Exception e) {
            e.printStackTrace();
        }

        double distanceKm = calculator.getDrivingDistance(pickupLat,pickupLon,dropLat,dropLon);
        System.out.println("📏 Distance (km): " + distanceKm);

        // Save booking first
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setPickupAddress(pickup);
        booking.setDropAddress(drop);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setBookingId("MOVE" + System.currentTimeMillis());
        booking.setDistanceInKm(distanceKm);

        booking = bookingRepo.save(booking); // must be saved first

        // Associate furniture items
        List<FurnitureItem> items = furnitureRepo.findByUserId(user.getId());
        for (FurnitureItem item : items) {
            item.setBooking(booking);
        }
        furnitureRepo.saveAll(items); // now update them

        booking.setFurnitureItems(items);
        booking.setTotalWeight(items.stream().mapToDouble(FurnitureItem::getWeight).sum());

        // Calculate costs
        double totalWeight = booking.getTotalWeight();
        String vehicleType;
        double perKmRate;

        if (totalWeight <= 300.00) {
            vehicleType = "Mini Truck";
            perKmRate = 15;
        } else if (totalWeight <= 1000.00) {
            vehicleType = "Tempo";
            perKmRate = 25;
        } else {
            vehicleType = "Large Truck";
            perKmRate = 33;
        }

        double transportCost = distanceKm * perKmRate;
        int totalQuantity = (int)items.stream().mapToDouble(FurnitureItem::getQuantity).sum();
        double packingCharges = totalQuantity * 100;

        booking.setTransportCost(transportCost);
        booking.setPackingCost(packingCharges);
        booking.setTotalCost(transportCost + packingCharges); // assuming service cost not used
        booking.setStatus("confirmed");

        bookingRepo.save(booking); // final update
        BookingReceiptGenerator.generateReceipt(booking);

        String pdfPath = "receipts/" + booking.getBookingId() + ".pdf";
        String subject = "Your MigrEase Booking Receipt - " + booking.getBookingId();
        String userName = booking.getUser().getName();
        String userEmail = booking.getUser().getEmail();

        emailService.sendBookingReceipt(subject, userName, userEmail, pdfPath, booking.getBookingId());

        return "redirect:/user/dashboard";
    }

    @GetMapping("/my-bookings")
    public String listBookings(Model model, Principal principal) {
        User user = userServices.findByEmail(principal.getName()).orElseThrow();
        List<Booking> bookings = bookingRepo.findByUser(user);
        model.addAttribute("bookings", bookings);
        return "user/booking/my-bookings";
    }

    @GetMapping("/booking-details/{bookingId}")
    public String showBookingDetails(@PathVariable String bookingId, Model model, Principal principal) {
        User user = userServices.findByEmail(principal.getName()).orElseThrow();

        // Filter bookings of this user by ID
        List<Booking> userBookings = bookingRepo.findByUser(user);
        Booking booking = userBookings.stream()
                .filter(b -> bookingId.equals(b.getBookingId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        double totalWeight = booking.getFurnitureItems()
                .stream()
                .mapToDouble(f -> f.getWeight())
                .sum();

        model.addAttribute("booking", booking);
        model.addAttribute("totalWeight", totalWeight);

        return "user/booking/booking-details";
    }

    @GetMapping("/track-booking/{bookingId}")
    public String trackBooking(@PathVariable String bookingId, Model model) {
        Booking booking = bookingRepo.findByBookingId(bookingId).orElseThrow();

        // Full pickup address string
        String fullPickupAddress = booking.getPickupAddress();

        logger.info(fullPickupAddress);
        model.addAttribute("booking", booking);
        model.addAttribute("fullPickupAddress", fullPickupAddress); // for JS use

        return "user/tracking/track-booking";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable("id") String bookingId,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        Optional<Booking> bookingOpt = bookingRepo.findByBookingId(bookingId);

        if (bookingOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Booking not found.");
            return "redirect:/user/my-bookings";
        }

        Booking booking = bookingOpt.get();

        // Ensure the booking belongs to the logged-in user
        String userEmail = principal.getName();
        if (!booking.getUser().getEmail().equals(userEmail)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to cancel this booking.");
            return "redirect:/user/my-bookings";
        }

        // Allow cancel only if status is not CANCELLED or COMPLETED
        String status = booking.getStatus().toUpperCase();
        if (status.equals("CANCELLED") || status.equals("COMPLETED")) {
            redirectAttributes.addFlashAttribute("error", "This booking cannot be cancelled.");
            return "redirect:/user/my-bookings";
        }

        // Perform cancellation
        booking.setStatus("CANCELLED");
        bookingRepo.save(booking);

        redirectAttributes.addFlashAttribute("message", "Booking cancelled successfully.");
        return "redirect:/user/my-bookings";
    }
}