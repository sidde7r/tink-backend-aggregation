package se.tink.backend.aggregation.agents.nxgen.es.banks.popular;

import com.google.common.collect.ImmutableList;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator.BancoPopularAuthenticator;
import se.tink.backend.aggregation.log.LogMasker;
import se.tink.backend.aggregation.log.LogMasker.LoggingMode;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.utils.ClientConfigurationStringMaskerBuilder;
import se.tink.backend.aggregation.utils.CredentialsStringMaskerBuilder;

public class BancoPopularTestBase {

    protected Credentials credentials;
    protected BancoPopularApiClient bankClient;
    protected BancoPopularAuthenticator authenticator;
    protected BancoPopularPersistentStorage persistentStorage;

    protected String user = BancoPopularTestConfig.USERNAME;
    protected String password = BancoPopularTestConfig.PASSWORD;

    protected void authenticate() throws Exception {
        authenticator.authenticate(user, password);
    }

    protected void setup() {
        credentials = new Credentials();
        AgentContext context = new AgentTestContext(credentials);
        TinkHttpClient client =
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
                                                                .CredentialsProperty.PASSWORD,
                                                        CredentialsStringMaskerBuilder
                                                                .CredentialsProperty.SECRET_KEY,
                                                        CredentialsStringMaskerBuilder
                                                                .CredentialsProperty
                                                                .SENSITIVE_PAYLOAD,
                                                        CredentialsStringMaskerBuilder
                                                                .CredentialsProperty.USERNAME)))
                                .addStringMaskerBuilder(
                                        new ClientConfigurationStringMaskerBuilder(
                                                context.getAgentConfigurationController()
                                                        .getSecretValues()))
                                .build(),
                        LoggingMode.LOGGING_MASKER_COVERS_SECRETS);
        client.setDebugOutput(true);

        persistentStorage = new BancoPopularPersistentStorage(new PersistentStorage());
        bankClient = new BancoPopularApiClient(client, new SessionStorage());
        authenticator = new BancoPopularAuthenticator(bankClient, persistentStorage);
    }
}
