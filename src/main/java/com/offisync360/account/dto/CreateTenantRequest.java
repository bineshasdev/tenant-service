package com.offisync360.account.dto;

import lombok.Data;

@Data
public class CreateTenantRequest {

    private String code;
    private String name;
    private String adminEmail;
    private String country;
    private String domain;
    private String phone; 
    private String state; 
    private String pincode;
    private String locale; 
    private String currency;

    private String subscriptionCode;
}
