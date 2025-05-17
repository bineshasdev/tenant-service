package com.offisync360.account.exception;

import org.springframework.http.HttpStatus;

public class LocalizedException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;
    private final HttpStatus status;

    public LocalizedException(String messageKey, HttpStatus status, Object... args) {
        this.messageKey = messageKey;
        this.args = args;
        this.status = status;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArgs() {
        return args;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
