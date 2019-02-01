package se.tink.backend.aggregation.agents.nxgen.fi.banks.poppankki.authenticator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.poppankki.PopPankkiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.poppankki.PopPankkiTestConfig;
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

public class PopPankkiKeyCardAuthenticatorTest extends NextGenerationBaseAgentTest {
    private static final AggregationLogger LOGGER = new AggregationLogger(PopPankkiKeyCardAuthenticatorTest.class);
    String username;
    String password;
    private Credentials credentials;
    private SamlinkPersistentStorage persistentStorage;
    private String keyCardCode;

    public PopPankkiKeyCardAuthenticatorTest() {
        super(PopPankkiKeyCardAuthenticatorTest.class);
    }

    @Before
    public void setUp() throws Exception {
        username = PopPankkiTestConfig.USERNAME;
        password = PopPankkiTestConfig.PASSWORD;

        credentials = new Credentials();
        persistentStorage = new SamlinkPersistentStorage(new PersistentStorage());
        credentials.setField(Field.Key.USERNAME, username);
        credentials.setField(Field.Key.PASSWORD, password);
    }

    @Test
    public void authenticate_HappyFlow() throws Exception {
        keyCardCode = "Intercept me! Set breakpoint at keyCardAuthenticator#authenticate()";

        authenticate();

        Assert.assertNotNull("No device id", persistentStorage.getDeviceId());
        Assert.assertNotNull("No device token", persistentStorage.getDeviceToken());

        LOGGER.info(String.format("Logged in and device registered: \ndevice id\n%s\ndevice token\n%s",
                persistentStorage.getDeviceId(),
                persistentStorage.getDeviceToken()
        ));
    }

    @Test(expected = LoginException.class)
    public void authenticate_InvalidKeyCardCode() throws Exception {
        keyCardCode = "123456"; // Not expected on key card

        authenticate();
    }

    private void authenticate() throws AuthenticationException, AuthorizationException {
        AgentContext context = new AgentTestContext(credentials);
        TinkHttpClient client = new TinkHttpClient(context.getAggregatorInfo(), context.getMetricRegistry(),
                context.getLogOutputStream(), null, null);
        client.setDebugOutput(true);
        SamlinkSessionStorage sessionStorage = new SamlinkSessionStorage(new SessionStorage());
        SamlinkApiClient bankClient = new SamlinkApiClient(client, sessionStorage, new SamlinkConfiguration(
                PopPankkiConstants.Url.BASE));
        SamlinkKeyCardAuthenticator keyCardAuthenticator = new SamlinkKeyCardAuthenticator(bankClient,
                persistentStorage, credentials);

        KeyCardInitValues keyCardInitValues = keyCardAuthenticator.init(username, password);
        LOGGER.info(
                String.format("KeyCard init values: from key card: %s use key code: %s", keyCardInitValues.getCardId(),
                        keyCardInitValues.getCardIndex()));
        keyCardAuthenticator.authenticate(keyCardCode);
    }

    @Override
    public String getCurrency() {
        return CurrencyConstants.FI.getCode();
    }
}
