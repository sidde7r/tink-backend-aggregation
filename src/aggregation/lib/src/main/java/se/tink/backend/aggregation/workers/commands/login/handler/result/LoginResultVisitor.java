package se.tink.backend.aggregation.workers.commands.login.handler.result;

public interface LoginResultVisitor {

    void visit(final LoginSuccessResult successResult);

    void visit(final LoginAuthorizationErrorResult authorizationErrorResult);

    void visit(final LoginAuthenticationErrorResult authenticationErrorResult);

    void visit(final LoginBankIdErrorResult bankIdErrorResult);

    void visit(final LoginBankServiceEroroResult bankServiceErrorResult);

    void visit(final LoginUnknownErrorResult unknownErrorResult);

    void visit(final AgentPlatformLoginErrorResult loginErrorResult);
}
