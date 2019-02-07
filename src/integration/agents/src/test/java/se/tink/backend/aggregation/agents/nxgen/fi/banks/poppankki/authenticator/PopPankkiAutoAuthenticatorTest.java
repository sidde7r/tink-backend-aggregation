package se.tink.backend.aggregation.agents.nxgen.fi.banks.poppankki.authenticator;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.poppankki.PopPankkiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.poppankki.PopPankkiTestConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.SamlinkAutoAuthenticator;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgentTest;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.utils.CurrencyConstants;

public class PopPankkiAutoAuthenticatorTest extends NextGenerationAgentTest {
    private static final AggregationLogger LOGGER = new AggregationLogger(PopPankkiAutoAuthenticatorTest.class);

    private Credentials credentials;
    private SamlinkPersistentStorage persistentStorage;

    public PopPankkiAutoAuthenticatorTest() {
        super(PopPankkiAutoAuthenticatorTest.class);
    }

    @Before
    public void setUp() throws Exception {
        String username = PopPankkiTestConfig.USERNAME;
        String password = PopPankkiTestConfig.PASSWORD;
        String deviceId = PopPankkiTestConfig.DEVICE_ID;
        String deviceToken = PopPankkiTestConfig.DEVICE_TOKEN;
        credentials = new Credentials();
        persistentStorage = new SamlinkPersistentStorage(new PersistentStorage());
        credentials.setField(Field.Key.USERNAME, username);
        credentials.setField(Field.Key.PASSWORD, password);
        persistentStorage.putDeviceId(deviceId);
        persistentStorage.putDeviceToken(deviceToken);
    }

    @Test
    public void testAuthenticate() throws Exception {
        AgentContext context = new AgentTestContext(credentials);
        TinkHttpClient client = new TinkHttpClient(context.getAggregatorInfo(), context.getMetricRegistry(),
                context.getLogOutputStream(), null, null);
        client.setDebugOutput(true);
        SamlinkSessionStorage sessionStorage = new SamlinkSessionStorage(new SessionStorage());
        SamlinkApiClient bankClient = new SamlinkApiClient(client, sessionStorage,
                new SamlinkConfiguration(PopPankkiConstants.Url.BASE));
        SamlinkAutoAuthenticator autoAuthenticator = new SamlinkAutoAuthenticator(bankClient, persistentStorage,
                credentials);

        autoAuthenticator.autoAuthenticate();

        LOGGER.info(String.format("Logged in with registered device: \ndevice id\n%s\ndevice token\n%s",
                persistentStorage.getDeviceId(),
                persistentStorage.getDeviceToken()
        ));
    }

    @Override
    public String getCurrency() {
        return CurrencyConstants.FI.getCode();
    }
}
