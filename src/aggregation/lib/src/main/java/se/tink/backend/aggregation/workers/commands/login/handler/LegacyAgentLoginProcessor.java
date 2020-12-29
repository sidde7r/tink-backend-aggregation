package se.tink.backend.aggregation.workers.commands.login.handler;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginAuthenticationErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginAuthorizationErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginBankIdErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginBankServiceEroroResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginUnknownErrorResult;

public class LegacyAgentLoginProcessor {

    static LoginResult create(final LoginExecutor loginExecutor) {
        try {
            return loginExecutor.execute();
        } catch (BankIdException bankIdException) {
            return new LoginBankIdErrorResult(bankIdException);
        } catch (AuthenticationException authenticationException) {
            return new LoginAuthenticationErrorResult(authenticationException);
        } catch (AuthorizationException authorizationException) {
            return new LoginAuthorizationErrorResult(authorizationException);
        } catch (BankServiceException bankServiceException) {
            return new LoginBankServiceEroroResult(bankServiceException);
        } catch (Exception ex) {
            return new LoginUnknownErrorResult(ex);
        }
    }

    interface LoginExecutor {
        LoginResult execute() throws Exception;
    }
}
