package se.tink.backend.aggregation.workers.commands.login.handler.result;

import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentPlatformAuthenticationProcessException;

public class AgentPlatformLoginErrorResult
        extends LoginFailedAbstractResult<AgentPlatformAuthenticationProcessException> {

    public AgentPlatformLoginErrorResult(AgentPlatformAuthenticationProcessException exception) {
        super(exception);
    }

    @Override
    public void accept(LoginResultVisitor visitor) {
        visitor.visit(this);
    }
}
