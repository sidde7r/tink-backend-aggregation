package se.tink.backend.aggregation.workers.commands.login.handler.result;

public class LoginUnknownErrorResult extends LoginFailedAbstractResult<Exception> {

    public LoginUnknownErrorResult(Exception exception) {
        super(exception);
    }

    @Override
    public void accept(LoginResultVisitor visitor) {
        visitor.visit(this);
    }
}
