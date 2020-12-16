package se.tink.backend.aggregation.workers.commands.login.handler.result;

import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

public class LoginAuthorizationErrorResult
        extends LoginFailedAbstractResult<AuthorizationException> {

    public LoginAuthorizationErrorResult(AuthorizationException exception) {
        super(exception);
    }

    @Override
    public void accept(LoginResultVisitor visitor) {
        visitor.visit(this);
    }
}
