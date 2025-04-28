package com.offisync360.account.utils;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class LocalizedMessageResolver {
 
     
    private final MessageSource messageSource;

    public String getMessage(String key, Object[] args, Locale locale) {
        return messageSource.getMessage(key, args, locale);
    }
}
