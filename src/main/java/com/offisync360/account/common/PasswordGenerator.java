package com.offisync360.account.common;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class PasswordGenerator {

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    private static final int PASSWORD_LENGTH = 16;

    public String generateStrongPassword() {
        String combinedChars = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL_CHARS;
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);

        // Ensure at least one character from each character set
        sb.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        sb.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        sb.append(NUMBER.charAt(random.nextInt(NUMBER.length())));
        sb.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));
        
        // Fill remaining with random characters
        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            sb.append(combinedChars.charAt(random.nextInt(combinedChars.length())));
        }
        
        // Shuffle the characters
        String shuffled = shuffleString(sb.toString());
        
        return shuffled;
    }

    private String shuffleString(String input) {
        List<Character> characters = input.chars()
            .mapToObj(c -> (char) c)
            .collect(Collectors.toList());
        Collections.shuffle(characters);
        return characters.stream()
            .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
            .toString();
    }
}