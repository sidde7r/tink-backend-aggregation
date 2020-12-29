package se.tink.backend.aggregation.workers.commands.login.handler.result;

public interface LoginResult {

    void accept(LoginResultVisitor visitor);
}
