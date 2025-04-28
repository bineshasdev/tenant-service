package com.offisync360.account.dto;

import lombok.Data;

@Data
public class UserRegistrationRequest {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    private String countryCode;
    private String tenantId; // Optional, if you want to associate the user with a specific tenant
    private String state; // Optional, if you want to associate the user with a specific state
    private String pincode; // Optional, if you want to associate the user with a specific pincode
    private String locale; // Optional, if you want to associate the user with a specific locale

    private String currency; // Optional, if you want to associate the user with a specific currency
    private String subscriptionCode; // Optional, if you want to associate the user with a specific subscription code
    private String status; // Optional, if you want to set the user's status (e.g., active, inactive)
    private String startDateEffective; // Optional, if you want to set the user's start date effective

    private String endDateEffective; // Optional, if you want to set the user's end date effective
    private String userType; // Optional, if you want to set the user's type (e.g., admin, regular user)    
}
