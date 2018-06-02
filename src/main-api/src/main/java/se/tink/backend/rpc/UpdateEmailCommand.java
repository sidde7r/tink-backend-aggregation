package se.tink.backend.rpc;

import se.tink.libraries.validation.exceptions.InvalidEmailException;
import se.tink.libraries.validation.validators.EmailValidator;

public class UpdateEmailCommand {
    private String email;

    public UpdateEmailCommand(String email) throws InvalidEmailException {
        EmailValidator.validate(email);
        this.email = email;
    }

    public String getUsername() {
        return email;
    }
}
