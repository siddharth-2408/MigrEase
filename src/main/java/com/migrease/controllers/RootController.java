package com.migrease.controllers;

import com.migrease.model.Address;
import com.migrease.model.User;
import com.migrease.repository.AddressRepository;
import com.migrease.service.UserServices;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class RootController {

    private final Logger logger = LoggerFactory.getLogger(RootController.class);

    @Autowired
    private UserServices userServices;

    @Autowired
    private AddressRepository addressRepo;

    @ModelAttribute
    public void addUser(Authentication authentication, Model model) {
        if (authentication == null) return;

        String email = authentication.getName();
        Optional<User> userOpt = userServices.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("loggedUser", user);

            // Safe address check
            Optional<Address> addressOpt = addressRepo.findByUser(user);
            addressOpt.ifPresent(address -> model.addAttribute("address", address));
        } else {
            logger.warn("User not found in DB for email: {}", email);
            model.addAttribute("loggedUser", null);
        }
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
