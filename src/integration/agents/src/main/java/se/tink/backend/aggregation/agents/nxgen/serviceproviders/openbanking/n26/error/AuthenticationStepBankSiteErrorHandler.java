package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.error;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;

@RequiredArgsConstructor
public abstract class AuthenticationStepBankSiteErrorHandler<T extends AgentAuthenticationRequest> {

    private final N26BankSiteErrorDiscoverer errorDiscoverer;

    public AgentAuthenticationResult executeWithHandling(T authenticationProcessRequest) {
        try {
            return execute(authenticationProcessRequest);
        } catch (HttpClientException ex) {
            if (errorDiscoverer.isBankSiteError(ex)) {
                return new AgentFailedAuthenticationResult(
                        ErrorFactory.createServerTemporaryUnavailableError(ex.getMessage()),
                        authenticationProcessRequest.getAuthenticationPersistedData());
            }
            throw ex;
        }
    }

    protected abstract AgentAuthenticationResult execute(T authenticationProcessRequest);
}
