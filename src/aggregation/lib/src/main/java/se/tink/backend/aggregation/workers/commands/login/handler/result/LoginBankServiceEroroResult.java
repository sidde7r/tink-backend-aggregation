package se.tink.backend.aggregation.workers.commands.login.handler.result;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;

public class LoginBankServiceEroroResult extends LoginFailedAbstractResult<BankServiceException> {

    public LoginBankServiceEroroResult(BankServiceException exception) {
        super(exception);
    }

    @Override
    public void accept(LoginResultVisitor visitor) {
        visitor.visit(this);
    }
}
