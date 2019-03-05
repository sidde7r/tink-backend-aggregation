package se.tink.backend.aggregation.agents.nxgen.mx;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentHelper;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BBVAConstants;

@Ignore
public class BBVAAgentTest {
    private final String PHONE_NUMBER = "";
    private final String PASSWORD = "";
    private final String CARD_NUMBER = "";

    private final ArgumentHelper helper = new ArgumentHelper(
            "tink.username",
            "tink.password",
            "tink.market",
            "tink.provider");

    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder("mx", "mx-bbva-password")
                .addCredentialField(Field.Key.USERNAME, helper.get("phoneNumber"))
                .addCredentialField(Field.Key.PASSWORD, helper.get("password"))
                .addCredentialField(BBVAConstants.FIELDS.CARD_NUMBER, helper.get("cardNumber"))
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }

}
