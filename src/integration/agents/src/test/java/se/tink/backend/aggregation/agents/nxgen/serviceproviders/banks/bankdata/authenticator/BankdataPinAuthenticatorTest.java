package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.framework.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataTestConfig;
import se.tink.backend.aggregation.log.LogMasker.LoggingMode;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class BankdataPinAuthenticatorTest {
    private boolean debugOutput;
    private String bankNo;
    private String username;
    private String password;
    private BankdataApiClient bankClient;

    @Before
    public void setUp() throws Exception {
        username = BankdataTestConfig.USERNAME;
        password = BankdataTestConfig.PASSWORD;
        bankNo = BankdataTestConfig.BANK_NO;
        debugOutput = BankdataTestConfig.DEBUG_OUTPUT;

        Credentials credentials = new Credentials();
        AgentContext context = new AgentTestContext(credentials);
        TinkHttpClient client =
                new LegacyTinkHttpClient(
                        context.getAggregatorInfo(),
                        context.getMetricRegistry(),
                        context.getLogOutputStream(),
                        null,
                        null,
                        context.getLogMasker(),
                        LoggingMode.LOGGING_MASKER_COVERS_SECRETS);
        client.setDebugOutput(debugOutput);
        Provider provider = new Provider();
        provider.setPayload(bankNo);
        bankClient = new BankdataApiClient(client, provider);
    }

    @Test
    public void authenticate() throws Exception {
        BankdataPinAuthenticator authenticator = new BankdataPinAuthenticator(bankClient);
        authenticator.authenticate(username, password);
    }
}
