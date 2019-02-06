package se.tink.backend.aggregation.agents.nxgen.mx;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BBVAConstants;

@Ignore
public class BBVAAgentTest {
    private final String PHONE_NUMBER = "";
    private final String PASSWORD = "";
    private final String CARD_NUMBER = "";

    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder("mx", "mx-bbva-password")
                .addCredentialField(Field.Key.USERNAME, PHONE_NUMBER)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .addCredentialField(BBVAConstants.FIELDS.CARD_NUMBER, CARD_NUMBER)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }

}
