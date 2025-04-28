package com.offisync360.account.exception;

import org.springframework.http.HttpStatus;

public class InvalidSubscriptionCodeException extends LocalizedException{
    public InvalidSubscriptionCodeException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
