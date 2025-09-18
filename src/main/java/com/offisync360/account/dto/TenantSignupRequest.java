package com.offisync360.account.dto;

import java.util.List;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull; 
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TenantSignupRequest {
 
    @NotBlank(message = "Company name is required") 
    @Size(min = 3, max = 30, message = "Company name must be between 3 and 30 characters")
    private String companyName;

    @NotBlank(message = "Display name is required")
    @Size(min = 3, max = 100, message = "Display name must be between 3 and 100 characters")
    private String displayName;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Admin email must be a valid email address")
    private String adminEmail;

    @NotBlank(message = "Admin first name is required")
    @Size(min = 2, max = 50, message = "Admin first name must be between 2 and 50 characters")
    private String adminFirstName;

    @NotBlank(message = "Admin last name is required")
    @Size(min = 2, max = 50, message = "Admin last name must be between 2 and 50 characters")
    private String adminLastName;

    @Size(max = 255, message = "Description must be less than 255 characters")
    private String description;

    @NotNull(message = "Terms acceptance is required")
    @AssertTrue(message = "You must accept the terms and conditions")
    private Boolean acceptTerms;

    // Optional fields for custom configuration
    private List<String> defaultRoles = List.of("user", "admin");
    private Boolean enableRegistration = false;
    private Boolean emailVerificationRequired = true;

    private String mobileNumber;

    private String country = "IN";

    private String locale = "en-GB";
}
