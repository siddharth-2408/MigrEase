package com.migrease.forms;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Register {

    @NotBlank(message = "Name is Required")
    @Size(min=3,message = "Minimum 3 characters")
    private String name;

    @NotBlank(message = "Email is Required")
    @Email(message = "Enter Valid Email")
    private String email;

    @NotBlank(message = "Password is Required")
    @Size(min = 3, message = "Minimum 3 characters")
    private String password;

    @NotBlank(message = "Phone is Required")
    @Size(min = 8, max = 12, message = "Should be between 8 and 12")
    private String phone;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;
}
