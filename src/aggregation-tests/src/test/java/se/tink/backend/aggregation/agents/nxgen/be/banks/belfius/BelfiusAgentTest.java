package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class BelfiusAgentTest {

    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("be", "be-belfius-cardreader")
            .addCredentialField(Field.Key.USERNAME, USERNAME)
            .addCredentialField(Field.Key.PASSWORD, PASSWORD)
            .loadCredentialsBefore(true)
            .saveCredentialsAfter(true);

    @Test
    public void testRefresh() throws Exception {
        builder.build()
                .testRefresh();
    }
}
