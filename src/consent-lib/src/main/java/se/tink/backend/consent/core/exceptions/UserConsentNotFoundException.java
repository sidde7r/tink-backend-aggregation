package se.tink.backend.consent.core.exceptions;

public class UserConsentNotFoundException extends Exception {
    public UserConsentNotFoundException(String userId, String id) {
        super(String.format("UserConsent not found (UserId = '%s', Id = '%s')", userId, id));
    }
}
