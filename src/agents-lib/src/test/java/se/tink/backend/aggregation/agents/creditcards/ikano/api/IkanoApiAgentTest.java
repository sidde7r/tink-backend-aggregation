package se.tink.backend.aggregation.agents.creditcards.ikano.api;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.backend.agents.rpc.Credentials;

import javax.naming.LimitExceededException;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class IkanoApiAgentTest {

    @Test(expected = LimitExceededException.class)
    public void throwsAfterMaximumNumberOfBankIdPolls() throws Exception {
        String reference = "1bbece7c-5ebd-4a34-a3a8-d72b99348e1e";
        IkanoApiAgent agent = createAgent();

        agent.pollBankIdSession(reference);
    }

    private IkanoApiAgent createAgent() throws Exception {
        CredentialsRequest request = mock(CredentialsRequest.class);
        AgentContext context = mock(AgentContext.class);
        IkanoApiClient apiClient = mock(IkanoApiClient.class);

        when(request.getCredentials()).thenReturn(new Credentials());
        when(apiClient.fetchBankIdSession(any(String.class))).thenReturn(false);

        return new IkanoApiAgent(request, context, new SignatureKeyPair(), apiClient, 1);
    }
}
