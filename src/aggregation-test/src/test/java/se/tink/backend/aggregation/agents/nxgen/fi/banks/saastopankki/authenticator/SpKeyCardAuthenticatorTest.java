package se.tink.backend.aggregation.agents.nxgen.fi.banks.saastopankki.authenticator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.saastopankki.SpConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.saastopankki.SpTestConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.SamlinkKeyCardAuthenticator;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.agents.nxgen.NextGenerationBaseAgentTest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.utils.CurrencyConstants;

public class SpKeyCardAuthenticatorTest extends NextGenerationBaseAgentTest {
    private static final AggregationLogger LOGGER = new AggregationLogger(SpKeyCardAuthenticatorTest.class);
    String username;
    String password;
    private Credentials credentials;
    private SamlinkPersistentStorage persistentStorage;

    public SpKeyCardAuthenticatorTest() {
        super(SpKeyCardAuthenticatorTest.class);
    }

    @Before
    public void setUp() throws Exception {
        username = SpTestConfig.USERNAME;
        password = SpTestConfig.PASSWORD;

        credentials = new Credentials();
        persistentStorage = new SamlinkPersistentStorage(new PersistentStorage());
        credentials.setField(Field.Key.USERNAME, username);
        credentials.setField(Field.Key.PASSWORD, password);
    }

    @Test
    public void testAuthenticate() throws Exception {
        AgentContext context = new AgentTestContext(credentials);
        TinkHttpClient client = new TinkHttpClient(context);
        client.setDebugOutput(true);
        SamlinkSessionStorage sessionStorage = new SamlinkSessionStorage(new SessionStorage());
        SamlinkApiClient bankClient = new SamlinkApiClient(client, sessionStorage, new SamlinkConfiguration(
                SpConstants.Url.BASE));
        SamlinkKeyCardAuthenticator keyCardAuthenticator = new SamlinkKeyCardAuthenticator(bankClient,
                persistentStorage, credentials);

        KeyCardInitValues keyCardInitValues = keyCardAuthenticator.init(username, password);
        LOGGER.info(
                String.format("KeyCard init values: from key card: %s use key code: %s", keyCardInitValues.getCardId(),
                        keyCardInitValues.getCardIndex()));
        String code = "";
        keyCardAuthenticator.authenticate(code);

        Assert.assertNotNull("No device id", persistentStorage.getDeviceId());
        Assert.assertNotNull("No device token", persistentStorage.getDeviceToken());

        LOGGER.info(String.format("Logged in and device registered: \ndevice id\n%s\ndevice token\n%s",
                persistentStorage.getDeviceId(),
                persistentStorage.getDeviceToken()
        ));
    }

    @Override
    public String getCurrency() {
        return CurrencyConstants.FI.getCode();
    }
}
