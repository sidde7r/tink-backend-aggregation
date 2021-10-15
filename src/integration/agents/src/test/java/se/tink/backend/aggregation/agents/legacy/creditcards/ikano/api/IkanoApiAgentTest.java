package se.tink.backend.aggregation.agents.creditcards.ikano.api;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.security.NoSuchAlgorithmException;
import org.assertj.core.util.Lists;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
import se.tink.backend.aggregation.nxgen.controllers.session.CredentialsPersistence;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.user.rpc.User;

public class IkanoApiAgentTest {
    private final ArgumentManager<UsernameArgumentEnum> manager =
            new ArgumentManager<>(UsernameArgumentEnum.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @Test
    public void testLoginAndRefresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("se", "preem-bankid")
                        .addCredentialField(
                                Field.Key.USERNAME, manager.get(UsernameArgumentEnum.USERNAME))
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);

        builder.build().testRefresh();
    }

    @Test(expected = BankIdException.class)
    public void throwsAfterMaximumNumberOfBankIdPolls() throws Exception {
        String reference = "1bbece7c-5ebd-4a34-a3a8-d72b99348e1e";
        IkanoApiClient client = mock(IkanoApiClient.class);
        IkanoApiAgent agent = createAgent(client);

        agent.pollBankIdSession(reference);
    }

    @Test
    public void shouldReturnKeepAliveTrueIfAbleToPostRequest() throws Exception {
        IkanoApiClient client = createApiClient();
        IkanoApiClient spyClient = spy(client);
        IkanoApiAgent agent = createAgent(spyClient);

        doReturn(Lists.emptyList()).when(spyClient).fetchAccounts();

        boolean result = agent.keepAlive();

        Assert.assertTrue(result);
    }

    @Test
    public void shouldReturnKeepAliveFalseIfExceptionIsThrown() throws Exception {
        IkanoApiClient client = createApiClient();
        IkanoApiClient spyClient = spy(client);
        IkanoApiAgent agent = createAgent(client);
        doReturn(false).when(spyClient).fetchBankIdSession(any(String.class));
        doThrow(IllegalStateException.class).when(spyClient).fetchAccounts();

        boolean result = agent.keepAlive();

        Assert.assertFalse(result);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    private IkanoApiClient createApiClient() throws NoSuchAlgorithmException {
        Credentials credentials = mock(Credentials.class);
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        return new IkanoApiClient(tinkHttpClient, credentials, "PREEM", "UserAgent");
    }

    private IkanoApiAgent createAgent(IkanoApiClient apiClient) throws Exception {
        Credentials credentials = mock(Credentials.class);
        CredentialsRequest request = getCredentialsRequest(credentials);
        CompositeAgentContext context = mock(CompositeAgentContext.class);
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        PersistentStorage persistentStorage = mock(PersistentStorage.class);
        SessionStorage sessionStorage = mock(SessionStorage.class);
        CredentialsPersistence credentialsPersistence =
                new CredentialsPersistence(
                        persistentStorage, sessionStorage, credentials, tinkHttpClient);

        return new IkanoApiAgent(
                request, context, apiClient, 1, credentialsPersistence, persistentStorage);
    }

    public CredentialsRequest getCredentialsRequest(Credentials credentials) {
        Provider provider = new Provider();
        provider.setClassName("banks.se.ikano.IkanoApiAgent");
        provider.setMarket("SE");
        UserAvailability userAvailability = mock(UserAvailability.class);
        User user = mock(User.class);
        return RefreshInformationRequest.builder()
                .user(user)
                .provider(provider)
                .credentials(credentials)
                .originatingUserIp("127.0.0.1")
                .userAvailability(userAvailability)
                .manual(false)
                .forceAuthenticate(false)
                .build();
    }
}
