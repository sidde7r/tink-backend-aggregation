package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class SdcAgentTest {

    private static final String TEST_SSN = "199011097236";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("se", "se-sdc-oauth2")
                        .addCredentialField(Key.LOGIN_INPUT, TEST_SSN)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
