package se.tink.backend.aggregation.agents.agentplatform.authentication.result;

import java.util.Optional;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentPlatformAuthenticationProcessError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentAuthenticationRequest;

public class AgentAuthenticationResultHandlingResult {

    private AgentPlatformAuthenticationProcessError authenticationError;

    private AgentAuthenticationRequest agentAuthenticationNextRequest;

    private AgentAuthenticationResultHandlingResult(
            AgentPlatformAuthenticationProcessError authenticationException) {
        this.authenticationError = authenticationException;
    }

    private AgentAuthenticationResultHandlingResult(
            AgentAuthenticationRequest agentAuthenticationNextRequest) {
        this.agentAuthenticationNextRequest = agentAuthenticationNextRequest;
    }

    private AgentAuthenticationResultHandlingResult() {}

    public Optional<AgentPlatformAuthenticationProcessError> getAuthenticationError() {
        return Optional.ofNullable(authenticationError);
    }

    public AgentAuthenticationRequest getAgentAuthenticationNextRequest() {
        return agentAuthenticationNextRequest;
    }

    public boolean isFinalResult() {
        return agentAuthenticationNextRequest == null;
    }

    public static AgentAuthenticationResultHandlingResult authenticationFailed(
            final AgentPlatformAuthenticationProcessError authenticationError) {
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
