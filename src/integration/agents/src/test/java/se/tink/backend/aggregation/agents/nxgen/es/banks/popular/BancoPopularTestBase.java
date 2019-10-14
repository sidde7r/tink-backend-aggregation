package se.tink.backend.aggregation.agents.nxgen.es.banks.popular;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator.BancoPopularAuthenticator;
import se.tink.backend.aggregation.log.LogMasker;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

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
                        new LogMasker(
                                credentials,
                                context.getAgentConfigurationController().getSecretValues()),
                        true);
        client.setDebugOutput(true);

        persistentStorage = new BancoPopularPersistentStorage(new PersistentStorage());
        bankClient = new BancoPopularApiClient(client, new SessionStorage());
        authenticator = new BancoPopularAuthenticator(bankClient, persistentStorage);
    }
}
