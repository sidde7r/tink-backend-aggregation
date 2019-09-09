package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import static org.mockito.Mockito.spy;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.NordeaNemIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.rpc.filter.NordeaDkFilter;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.NemidPasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaTestBase {
    protected String username;
    protected String password;
    protected NemidPasswordAuthenticationController authenticator;
    protected NordeaDkApiClient bankClient;
    protected Credentials credentials;
    protected AgentContext context;
    protected TinkHttpClient tinkHttpClient;

    protected void setUpTest() throws Exception {
        username = TestConfig.USERNAME;
        password = TestConfig.PASSWORD;

        credentials = new Credentials();
        credentials.setUserId("TEST-USER-ID");
        credentials.setUsername(username);
        credentials.setPassword(password);

        context = new AgentTestContext(credentials);

        tinkHttpClient =
                spy(
                        new TinkHttpClient(
                                context.getAggregatorInfo(),
                                context.getMetricRegistry(),
                                context.getLogOutputStream(),
                                null,
                                null));
        tinkHttpClient.setDebugOutput(TestConfig.CLIENT_DEBUG_OUTPUT);
        tinkHttpClient.addFilter(new NordeaDkFilter());

        NordeaDkSessionStorage sessionStorage = new NordeaDkSessionStorage(new SessionStorage());
        bankClient = new NordeaDkApiClient(sessionStorage, tinkHttpClient, credentials, "DK");
        NordeaNemIdAuthenticator nordeaNemIdAuthenticator =
                new NordeaNemIdAuthenticator(bankClient, sessionStorage);
        authenticator = new NemidPasswordAuthenticationController(nordeaNemIdAuthenticator);
    }

    protected void authenticateTestUser() throws Exception {
        authenticator.authenticate(username, password);
    }
}
