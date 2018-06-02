package se.tink.backend.core;

public class ApplicationFieldError {

    private String message;
    
    public ApplicationFieldError() {
        
    }

    public ApplicationFieldError(String message) {
        setMessage(message);
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
