package se.tink.backend.aggregation.agents.nxgen.it.openbanking.unicredit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class UnicreditAgentTest {

    private static final String TEST_PSU_ID_TYPE = "ALL";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("it", "it-unicredit-oauth2")
                        .addCredentialField(Key.ADDITIONAL_INFORMATION, TEST_PSU_ID_TYPE)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
