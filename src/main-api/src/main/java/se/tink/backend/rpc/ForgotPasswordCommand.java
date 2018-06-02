package se.tink.backend.rpc;

import java.util.Optional;
import se.tink.libraries.validation.exceptions.InvalidEmailException;
import se.tink.libraries.validation.validators.EmailValidator;

public class ForgotPasswordCommand {
    private String username;
    private Optional<String> remoteAddress;
    private Optional<String> userAgent;

    public ForgotPasswordCommand(String username, Optional<String> remoteAddress,
            Optional<String> userAgent) throws InvalidEmailException {
        EmailValidator.validate(username);

        this.username = username;
        this.remoteAddress = remoteAddress;
        this.userAgent = userAgent;
    }

    public String getUsername() {
        return username;
    }

    public Optional<String> getRemoteAddress() {
        return remoteAddress;
    }

    public Optional<String> getUserAgent() {
        return userAgent;
    }
}
