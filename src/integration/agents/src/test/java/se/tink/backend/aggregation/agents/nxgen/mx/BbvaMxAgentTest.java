package se.tink.backend.aggregation.agents.nxgen.mx;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentHelper;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BbvaMxConstants;

@Ignore
public class BbvaMxAgentTest {

    private final ArgumentHelper helper =
            new ArgumentHelper("tink.username", "tink.password", "tink.market", "tink.provider");

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentHelper.afterClass();
    }

    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder("mx", "mx-bbva-password")
                .addCredentialField(Field.Key.USERNAME, helper.get("phoneNumber"))
                .addCredentialField(Field.Key.PASSWORD, helper.get("password"))
                .addCredentialField(BbvaMxConstants.FIELDS.CARD_NUMBER, helper.get("cardNumber"))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}
