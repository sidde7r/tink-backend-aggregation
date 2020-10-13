package se.tink.backend.aggregation.agents.agentplatform.authentication.result;

import java.util.Optional;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentAuthenticationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentAuthenticationRequest;

public class AgentAuthenticationResultHandlingResult {

    private AgentAuthenticationError authenticationError;

    private AgentAuthenticationRequest agentAuthenticationNextRequest;

    private AgentAuthenticationResultHandlingResult(
            AgentAuthenticationError authenticationException) {
        this.authenticationError = authenticationException;
    }

    private AgentAuthenticationResultHandlingResult(
            AgentAuthenticationRequest agentAuthenticationNextRequest) {
        this.agentAuthenticationNextRequest = agentAuthenticationNextRequest;
    }

    private AgentAuthenticationResultHandlingResult() {}

    public Optional<AgentAuthenticationError> getAuthenticationError() {
        return Optional.ofNullable(authenticationError);
    }

    public AgentAuthenticationRequest getAgentAuthenticationNextRequest() {
        return agentAuthenticationNextRequest;
    }

    public boolean isFinalResult() {
        return agentAuthenticationNextRequest == null;
    }

    public static AgentAuthenticationResultHandlingResult authenticationFailed(
            final AgentAuthenticationError authenticationError) {
        return new AgentAuthenticationResultHandlingResult(authenticationError);
    }

    public static AgentAuthenticationResultHandlingResult nextAuthenticationRequest(
            final AgentAuthenticationRequest nextAuthenticationRequest) {
        return new AgentAuthenticationResultHandlingResult(nextAuthenticationRequest);
    }

    public static AgentAuthenticationResultHandlingResult authenticationSuccess() {
        return new AgentAuthenticationResultHandlingResult();
    }
}
