package com.offisync360.account.aop;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity; 
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.offisync360.account.dto.ErrorResponse;
import com.offisync360.account.exception.BusinessValidationException;
import com.offisync360.account.exception.InvalidSubscriptionCodeException;
import com.offisync360.account.exception.LocalizedException;
import com.offisync360.account.exception.SubscriptionLimitExceededException;
import com.offisync360.account.utils.LocalizedMessageResolver;

import lombok.AllArgsConstructor;

@ControllerAdvice
@AllArgsConstructor
public class GlobalExceptionHandler {

    private final LocalizedMessageResolver resolver;

    @ExceptionHandler(SubscriptionLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handleSubscriptionLimit(SubscriptionLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }
 
    @ExceptionHandler(LocalizedException.class)
    public ResponseEntity<ErrorResponse> handleBusinessValidationException(
        LocalizedException ex, WebRequest request) {
           
         /*   String localizedMsg =  ex.getMessage();
            try{
                 localizedMsg =   resolver.getMessage(ex.getMessageKey(), ex.getArgs(), locale);
            } catch (Exception e) {
                // Handle the exception here, e.g., log it or rethrow it
            } */
   

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value()) 
                .message(ex.getMessage())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();

                if( ex instanceof LocalizedException) { 
                   
                    try{
                        var localizedMsg =   resolver.getMessage(ex.getMessageKey(), ex.getArgs(), Locale.getDefault());
                        errorResponse.setMessage(localizedMsg);
                        errorResponse.setError(localizedMsg);
                   } catch (Exception e) {
                       
                        errorResponse.setMessage(ex.getMessageKey());
                        errorResponse.setError(ex.getMessageKey());
                   }
                   
                } else if (ex instanceof InvalidSubscriptionCodeException) {
                    errorResponse.setError("Invalid Subscription Code Error");
                } else {
                    errorResponse.setError("Unknown Error");
                }
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }
}