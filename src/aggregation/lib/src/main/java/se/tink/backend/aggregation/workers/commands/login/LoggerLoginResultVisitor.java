package se.tink.backend.aggregation.workers.commands.login;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.workers.commands.login.handler.result.AgentPlatformLoginErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.ConnectivityExceptionErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginAuthenticationErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginAuthorizationErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginBankIdErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginBankServiceEroroResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginResultVisitor;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginSuccessResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginUnknownErrorResult;
import se.tink.connectivity.errors.ConnectivityErrorType;

@Slf4j
public class LoggerLoginResultVisitor implements LoginResultVisitor {

    @Override
    public void visit(LoginSuccessResult successResult) {
        log.info("Login success");
    }

    @Override
    public void visit(LoginAuthorizationErrorResult authorizationErrorResult) {
        log.info("Authorization error", authorizationErrorResult.getException());
    }

    @Override
    public void visit(LoginAuthenticationErrorResult authenticationErrorResult) {
        log.info("Authentication error", authenticationErrorResult.getException());
    }

    @Override
    public void visit(LoginBankIdErrorResult bankIdErrorResult) {
        log.info("BankId error", bankIdErrorResult.getException());
    }

    @Override
    public void visit(LoginBankServiceEroroResult bankServiceErrorResult) {
        log.info("BankService error", bankServiceErrorResult.getException());
    }

    @Override
    public void visit(LoginUnknownErrorResult unknownErrorResult) {
        log.error(
                unknownErrorResult.getException().getMessage(), unknownErrorResult.getException());
    }

    @Override
    public void visit(AgentPlatformLoginErrorResult loginErrorResult) {
        log.info(
                loginErrorResult
                        .getException()
                        .getSourceAgentPlatformError()
                        .getDetails()
                        .getErrorMessage(),
                loginErrorResult.getException());
    }

    @Override
    public void visit(ConnectivityExceptionErrorResult connectivityExceptionErrorResult) {
        ConnectivityErrorType errorType =
                connectivityExceptionErrorResult.getException().getError().getType();
        String logMessage =
                String.format(
                        "%s.%s",
                        errorType.name(),
                        connectivityExceptionErrorResult.getException().getError().getDetails());
        switch (errorType) {
            case USER_LOGIN_ERROR:
            case AUTHORIZATION_ERROR:
            case PROVIDER_ERROR:
                log.info(logMessage, connectivityExceptionErrorResult.getException());
                break;
            default:
                log.error(logMessage, connectivityExceptionErrorResult.getException());
        }
    }
}
