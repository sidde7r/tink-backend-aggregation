package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class VolksbankAgentTest {

    private final String TEST_PSU_ID = "100002";
    private final String TEST_PSU_ID_TYPE = "disposer-nr";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("at", "at-volksbank-oauth2")
                        .addCredentialField(Key.LOGIN_INPUT, TEST_PSU_ID)
                        .addCredentialField(Key.LOGIN_DESCRIPTION, TEST_PSU_ID_TYPE)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
