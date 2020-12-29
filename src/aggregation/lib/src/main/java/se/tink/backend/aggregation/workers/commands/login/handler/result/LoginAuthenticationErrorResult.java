package se.tink.backend.aggregation.workers.commands.login.handler.result;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;

public class LoginAuthenticationErrorResult
        extends LoginFailedAbstractResult<AuthenticationException> {

    public LoginAuthenticationErrorResult(AuthenticationException exception) {
        super(exception);
    }

    @Override
    public void accept(LoginResultVisitor visitor) {
        visitor.visit(this);
    }
}
