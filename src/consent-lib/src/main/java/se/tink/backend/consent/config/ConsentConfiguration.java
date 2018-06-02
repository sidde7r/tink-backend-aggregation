package se.tink.backend.consent.config;

import java.util.Optional;

public class ConsentConfiguration {
    private String signingKeyPath;

    public Optional<String> getSigningKeyPath() {
        return Optional.ofNullable(signingKeyPath);
    }
}
