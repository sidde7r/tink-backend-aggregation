package se.tink.backend.aggregation.agents.nxgen.fi.banks.poppankki.authenticator;

import java.lang.invoke.MethodHandles;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.framework.context.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.poppankki.PopPankkiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.poppankki.PopPankkiTestConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkV1Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.SamlinkKeyCardAuthenticator;
import se.tink.backend.aggregation.agents.utils.currency.CurrencyConstants;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl.LoggingMode;
import se.tink.backend.aggregation.nxgen.agents.agenttest.NextGenerationAgentTest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class PopPankkiKeyCardAuthenticatorTest extends NextGenerationAgentTest {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
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

        logger.info(
                String.format(
                        "Logged in and device registered: \ndevice id\n%s\ndevice token\n%s",
                        persistentStorage.getDeviceId(), persistentStorage.getDeviceToken()));
    }

    @Test(expected = LoginException.class)
    public void authenticate_InvalidKeyCardCode() throws Exception {
        keyCardCode = "123456"; // Not expected on key card

        authenticate();
    }

    private void authenticate() throws AuthenticationException, AuthorizationException {
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
        SamlinkSessionStorage sessionStorage = new SamlinkSessionStorage(new SessionStorage());
        SamlinkApiClient bankClient =
                new SamlinkApiClient(
                        client,
                        sessionStorage,
                        new SamlinkV1Configuration(PopPankkiConstants.Url.BASE));
        SamlinkKeyCardAuthenticator keyCardAuthenticator =
                new SamlinkKeyCardAuthenticator(bankClient, persistentStorage, credentials);

        KeyCardInitValues keyCardInitValues = keyCardAuthenticator.init(username, password);
        logger.info(
                String.format(
                        "KeyCard init values: from key card: %s use key code: %s",
                        keyCardInitValues.getCardId(), keyCardInitValues.getCardIndex()));
        keyCardAuthenticator.authenticate(keyCardCode);
    }

    @Override
    public String getCurrency() {
        return CurrencyConstants.FI.getCode();
    }
}
