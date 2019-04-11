package se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class SdcSeAgentTest {
    private static final String USERNAME = "";

    @Test
    public void testRegisterAndRefresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("se", "sparbankensyd-bankid")
                        .addCredentialField(Field.Key.USERNAME, USERNAME)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);

        builder.build().testRefresh();
    }
}
