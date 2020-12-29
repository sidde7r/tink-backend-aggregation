package se.tink.backend.aggregation.workers.commands.login.handler.result;

import se.tink.backend.aggregation.agents.exceptions.BankIdException;

public class LoginBankIdErrorResult extends LoginFailedAbstractResult<BankIdException> {

    public LoginBankIdErrorResult(BankIdException exception) {
        super(exception);
    }

    @Override
    public void accept(LoginResultVisitor visitor) {
        visitor.visit(this);
    }
}
