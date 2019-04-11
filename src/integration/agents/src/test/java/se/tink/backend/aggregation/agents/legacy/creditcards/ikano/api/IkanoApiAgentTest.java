package se.tink.backend.aggregation.agents.creditcards.ikano.api;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

@Ignore
public class IkanoApiAgentTest {
    private final String USERNAME = "";

    @Test
    public void testLoginAndRefresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("se", "preem-bankid")
                        .addCredentialField(Field.Key.USERNAME, USERNAME)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);

        builder.build().testRefresh();
    }

    @Test(expected = BankIdException.class)
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
