package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;

public class LoginExceptionImpl extends LoginException {
    private LoginError error;

    public LoginExceptionImpl(LoginError error) {
        super(error);
        this.error = error;
    }

    @Override
    public LoginError getError() {
        return error;
    }
}
