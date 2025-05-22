package com.migrease.controllers;

import com.migrease.forms.Register;
import com.migrease.helper.Message;
import com.migrease.model.SupportRequests;
import com.migrease.model.User;
import com.migrease.service.SupportService;
import com.migrease.service.UserServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PageController {

    @Autowired
    private UserServices userServices;

    @Autowired
    private SupportService supportService;

    @GetMapping("/")
    public String index() {
        return "index/index";
    }

    @GetMapping("/services")
    public String services() {
        return "services-type/services-type";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact/contact";
    }

    @GetMapping("/availability")
    public String availability() {
        return "check-availability/availability";
    }

    @GetMapping("/login")
    public String login() {
        return "login/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("userForm", new Register());
        return "registration/registration";
    }

    @PostMapping("/do-register")
    public String doRegister(@Valid @ModelAttribute("userForm") Register userForm,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if(bindingResult.hasErrors()) {
            System.out.println("Validation errors: " + bindingResult.getAllErrors());
            model.addAttribute("userForm", userForm);
            return "registration/registration";
        }

        if (!userForm.getPassword().equals(userForm.getConfirmPassword())) {
            model.addAttribute("userForm", userForm);
            bindingResult.rejectValue("confirmPassword", "error.userForm", "Passwords do not match");
            return "registration/registration";
        }

        User user = new User();
        user.setName(userForm.getName());
        user.setPassword(userForm.getPassword());
        user.setPhone(userForm.getPhone());
        user.setEmail(userForm.getEmail());

        User savedUser = userServices.saveUser(user);
        redirectAttributes.addFlashAttribute("message",
                new Message("Registered Successfully!! Please Proceed with Login..",
                        "success-alert"));
        return "redirect:/login";
    }

    @GetMapping("/support")
    public String showSupport(Model model) {
        model.addAttribute("supportRequest", new SupportRequests());
        return "support";
    }

    @PostMapping("/support")
    public String submitSupport(@ModelAttribute("supportRequest") SupportRequests supportRequest,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "support";
        }

        try {
            boolean exists = userServices.existsUserByEmail(supportRequest.getEmail());
            if (!exists) {
                redirectAttributes.addFlashAttribute("message",
                        new Message("User Does Not Exist","danger"));
                return "redirect:/support";
            }
            supportService.saveSupportRequest(supportRequest);
            redirectAttributes.addFlashAttribute("message",
                    new Message("Your support request has been submitted successfully. We'll get back to you soon!","success-alert"));
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message",
                    new Message("An error occurred while submitting your request. Please try again.","danger"));
            return "redirect:/support";
        }
    }
}
