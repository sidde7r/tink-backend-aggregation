package se.tink.backend.aggregation.agents.nxgen.de.banks.amex;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class AmericanExpressDEAgentTest {
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("de", "de-americanexpress-password")
                    .addCredentialField(Field.Key.USERNAME, USERNAME)
                    .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(false);

    @Test
    public void testRefresh() throws Exception {
        builder.build()
                .testRefresh();
    }
}
