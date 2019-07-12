package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class UnicreditAgentTest {

    private static final String TEST_PSU_ID_TYPE = "HVB_ONLINEBANKING";
    private static final String TEST_USERNAME = "bgdemo";
    private static final String TEST_PASSWORD = "bgpassword";
    private static final String TEST_OTP = "123456";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("de", "de-unicredit-oauth2")
                        .addCredentialField(Key.ADDITIONAL_INFORMATION, TEST_PSU_ID_TYPE)
                        .addCredentialField(Key.USERNAME, TEST_USERNAME)
                        .addCredentialField(Key.PASSWORD, TEST_PASSWORD)
                        .addCredentialField(Key.OTP_INPUT, TEST_OTP)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
