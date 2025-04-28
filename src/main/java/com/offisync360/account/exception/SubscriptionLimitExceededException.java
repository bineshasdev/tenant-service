package com.offisync360.account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN)
public class SubscriptionLimitExceededException extends RuntimeException {
    public SubscriptionLimitExceededException(String message) {
        super(message);
    }
}
