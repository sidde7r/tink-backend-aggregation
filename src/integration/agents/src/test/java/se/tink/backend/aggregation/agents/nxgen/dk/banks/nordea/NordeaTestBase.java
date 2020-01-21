package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import static org.mockito.Mockito.spy;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.CompositeAgentContext;
import se.tink.backend.aggregation.agents.framework.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.NordeaNemIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.rpc.filter.NordeaDkFilter;
import se.tink.backend.aggregation.logmasker.LogMasker.LoggingMode;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v1.NemidPasswordAuthenticationControllerV1;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaTestBase {
    protected String username;
    protected String password;
    protected NemidPasswordAuthenticationControllerV1 authenticator;
    protected NordeaDkApiClient bankClient;
    protected Credentials credentials;
    protected CompositeAgentContext context;
    protected TinkHttpClient tinkHttpClient;

    protected void setUpTest() {
        username = TestConfig.USERNAME;
        password = TestConfig.PASSWORD;

        credentials = new Credentials();
        credentials.setUserId("TEST-USER-ID");
        credentials.setUsername(username);
        credentials.setPassword(password);

        context = new AgentTestContext(credentials);

        tinkHttpClient =
                spy(
                        new LegacyTinkHttpClient(
                                context.getAggregatorInfo(),
                                context.getMetricRegistry(),
                                context.getLogOutputStream(),
                                null,
                                null,
                                context.getLogMasker(),
                                LoggingMode.LOGGING_MASKER_COVERS_SECRETS));
        tinkHttpClient.setDebugOutput(TestConfig.CLIENT_DEBUG_OUTPUT);
        tinkHttpClient.addFilter(new NordeaDkFilter());

        NordeaDkSessionStorage sessionStorage = new NordeaDkSessionStorage(new SessionStorage());
        bankClient = new NordeaDkApiClient(sessionStorage, tinkHttpClient, credentials, "DK");
        NordeaNemIdAuthenticator nordeaNemIdAuthenticator =
                new NordeaNemIdAuthenticator(bankClient, sessionStorage);
        authenticator = new NemidPasswordAuthenticationControllerV1(nordeaNemIdAuthenticator);
    }

    protected void authenticateTestUser() throws Exception {
        authenticator.authenticate(username, password);
    }
}
