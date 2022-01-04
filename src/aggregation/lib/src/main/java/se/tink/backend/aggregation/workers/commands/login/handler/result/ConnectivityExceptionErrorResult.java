package se.tink.backend.aggregation.workers.commands.login.handler.result;

import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;

public class ConnectivityExceptionErrorResult
        extends LoginFailedAbstractResult<ConnectivityException> {

    public ConnectivityExceptionErrorResult(ConnectivityException exception) {
        super(exception);
    }

    @Override
    public void accept(LoginResultVisitor visitor) {
        visitor.visit(this);
    }
}
