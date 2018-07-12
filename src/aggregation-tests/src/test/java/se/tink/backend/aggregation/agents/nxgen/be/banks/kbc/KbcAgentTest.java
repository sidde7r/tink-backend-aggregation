package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class KbcAgentTest {

    private static final String USERNAME = "";

    private static final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("be", "be-kbc-cardreader")
                    .addCredentialField(Field.Key.USERNAME, USERNAME)
                    .loadCredentialsBefore(true)
                    .saveCredentialsAfter(true);

    @Test
    public void testRefresh() throws Exception {
        builder.build()
                .testRefresh();
    }
}
