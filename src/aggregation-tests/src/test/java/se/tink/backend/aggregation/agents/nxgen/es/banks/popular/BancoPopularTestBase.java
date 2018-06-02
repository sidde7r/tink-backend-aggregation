package se.tink.backend.aggregation.agents.nxgen.es.banks.popular;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator.BancoPopularAuthenticator;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.Credentials;

public class BancoPopularTestBase {

    protected Credentials credentials;
    protected BancoPopularApiClient bankClient;
    protected BancoPopularAuthenticator authenticator;
    protected BancoPopularPersistenStorage persistentStorage;

    protected String user = BancoPopularTestConfig.USERNAME;
    protected String password = BancoPopularTestConfig.PASSWORD;


    protected void authenticate() throws Exception {
        authenticator.authenticate(user, password);
    }

    protected void setup() {
        credentials = new Credentials();
        AgentContext context = new AgentTestContext(null, credentials);
        TinkHttpClient client = new TinkHttpClient(context, credentials);
        client.setDebugOutput(true);

        persistentStorage = new BancoPopularPersistenStorage(new PersistentStorage());
        bankClient = new BancoPopularApiClient(client, new SessionStorage(), credentials);
        authenticator = new BancoPopularAuthenticator(bankClient, persistentStorage);
    }
}
