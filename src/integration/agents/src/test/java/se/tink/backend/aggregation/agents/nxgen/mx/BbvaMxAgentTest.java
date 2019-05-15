package se.tink.backend.aggregation.agents.nxgen.mx;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BbvaMxConstants;

public class BbvaMxAgentTest {

    private enum Arg {
        PHONE_NUMBER,
        CARD_NUMBER,
        PASSWORD,
        MARKET,
        PROVIDER,
    }

    private final ArgumentManager<Arg> helper = new ArgumentManager<>(Arg.values());

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder("mx", "mx-bbva-password")
                .addCredentialField(Field.Key.USERNAME, helper.get(Arg.PHONE_NUMBER))
                .addCredentialField(Field.Key.PASSWORD, helper.get(Arg.PASSWORD))
                .addCredentialField(BbvaMxConstants.FIELDS.CARD_NUMBER, helper.get(Arg.CARD_NUMBER))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}
