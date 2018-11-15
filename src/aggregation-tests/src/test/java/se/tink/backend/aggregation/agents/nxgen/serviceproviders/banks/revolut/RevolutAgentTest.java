package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

//@Ignore
public class RevolutAgentTest {

    private final String USERNAME = "+46724026264"; // phone number +{country code}{phone number}, eg. +46701234567
    private final String PASSWORD = "1234"; // four digits, eg. 1234

    @Test
    public void testLoginRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-revolut-password")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }
}
