package se.tink.backend.consent.core.exceptions;

public class ConsentNotFoundException extends Exception {
    public ConsentNotFoundException(String key, String version, String locale) {
        super(String.format("Consent not found (Key = '%s', Version = '%s', Locale = '%s')", key, version, locale));
    }
}
