package se.tink.backend.aggregation.workers.commands.login;

import lombok.Getter;
import se.tink.backend.aggregation.workers.commands.login.handler.result.AgentPlatformLoginErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginAuthenticationErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginAuthorizationErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginBankIdErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginBankServiceEroroResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginResultVisitor;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginSuccessResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginUnknownErrorResult;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

@Getter
public class AgentWorkerCommandResultLoginResultVisitor implements LoginResultVisitor {

    private AgentWorkerCommandResult result;

    @Override
    public void visit(LoginSuccessResult successResult) {
        result = AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void visit(LoginAuthorizationErrorResult authorizationErrorResult) {
        result = AgentWorkerCommandResult.ABORT;
    }

    @Override
    public void visit(LoginAuthenticationErrorResult authenticationErrorResult) {
        result = AgentWorkerCommandResult.ABORT;
    }

    @Override
    public void visit(LoginBankIdErrorResult bankIdErrorResult) {
        result = AgentWorkerCommandResult.ABORT;
    }

    @Override
    public void visit(LoginBankServiceEroroResult bankServiceErrorResult) {
        result = AgentWorkerCommandResult.ABORT;
    }

    @Override
    public void visit(LoginUnknownErrorResult unknownErrorResult) {
        result = AgentWorkerCommandResult.ABORT;
    }

    @Override
    public void visit(AgentPlatformLoginErrorResult loginErrorResult) {
        result = AgentWorkerCommandResult.ABORT;
    }
}
