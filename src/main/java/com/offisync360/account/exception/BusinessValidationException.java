package com.offisync360.account.exception;

import org.springframework.http.HttpStatus;

public class BusinessValidationException extends LocalizedException {

    public BusinessValidationException(String message) {
        super(message,  HttpStatus.BAD_REQUEST);
    }
}
