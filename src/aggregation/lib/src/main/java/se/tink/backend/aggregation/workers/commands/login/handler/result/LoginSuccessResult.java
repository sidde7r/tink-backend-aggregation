package se.tink.backend.aggregation.workers.commands.login.handler.result;

public class LoginSuccessResult implements LoginResult {

    @Override
    public void accept(LoginResultVisitor visitor) {
        visitor.visit(this);
    }
}
