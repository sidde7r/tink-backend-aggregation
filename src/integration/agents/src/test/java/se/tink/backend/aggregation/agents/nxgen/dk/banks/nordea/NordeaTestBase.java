package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import static org.mockito.Mockito.spy;

import com.google.common.collect.ImmutableList;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.NordeaNemIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.rpc.filter.NordeaDkFilter;
import se.tink.backend.aggregation.log.LogMasker;
import se.tink.backend.aggregation.log.LogMasker.LoggingMode;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.NemidPasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.utils.Base64Masker;
import se.tink.backend.aggregation.utils.ClientConfigurationStringMaskerBuilder;
import se.tink.backend.aggregation.utils.CredentialsStringMaskerBuilder;

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
                        new LegacyTinkHttpClient(
                                context.getAggregatorInfo(),
                                context.getMetricRegistry(),
                                context.getLogOutputStream(),
                                null,
                                null,
                                LogMasker.builder()
                                        .addStringMaskerBuilder(
                                                new CredentialsStringMaskerBuilder(
                                                        credentials,
                                                        ImmutableList.of(
                                                                CredentialsStringMaskerBuilder
                                                                        .CredentialsProperty
                                                                        .PASSWORD,
                                                                CredentialsStringMaskerBuilder
                                                                        .CredentialsProperty
                                                                        .SECRET_KEY,
                                                                CredentialsStringMaskerBuilder
                                                                        .CredentialsProperty
                                                                        .SENSITIVE_PAYLOAD,
                                                                CredentialsStringMaskerBuilder
                                                                        .CredentialsProperty
                                                                        .USERNAME)))
                                        .addStringMaskerBuilder(
                                                new ClientConfigurationStringMaskerBuilder(
                                                        context.getAgentConfigurationController()
                                                                .getSecretValues()))
                                        .addStringMaskerBuilder(
                                                new Base64Masker(
                                                        context.getAgentConfigurationController()
                                                                .getSecretValues()))
                                        .build(),
                                LoggingMode.LOGGING_MASKER_COVERS_SECRETS));
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
