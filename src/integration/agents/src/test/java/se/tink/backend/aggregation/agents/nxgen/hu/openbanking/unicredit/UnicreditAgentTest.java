package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class UnicreditAgentTest {

    private static final String TEST_PSU_ID_TYPE = "ALL";
    private static final String TEST_IBAN = "HU47109180010000007941740002";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("hu", "hu-unicredit-oauth2")
                        .addCredentialField(Key.ADDITIONAL_INFORMATION, TEST_PSU_ID_TYPE)
                        .addCredentialField(Key.LOGIN_INPUT, TEST_IBAN)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
