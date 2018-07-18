package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class IngAgentTest {

    private static final String USERNAME = "";
    private static final String CARD_ID = "";

    private static final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("be", "be-ing-cardreader")
                    .addCredentialField(Field.Key.USERNAME, USERNAME)
                    .addCredentialField("cardId", CARD_ID)
                    .loadCredentialsBefore(true)
                    .saveCredentialsAfter(true);

    @Test
    public void testRefresh() throws Exception {
        builder.build()
                .testRefresh();
    }
}
