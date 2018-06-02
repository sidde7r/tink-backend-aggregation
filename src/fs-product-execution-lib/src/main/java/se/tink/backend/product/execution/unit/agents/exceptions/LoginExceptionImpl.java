package se.tink.backend.product.execution.unit.agents.exceptions;

import se.tink.backend.product.execution.unit.agents.exceptions.errors.LoginError;

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
