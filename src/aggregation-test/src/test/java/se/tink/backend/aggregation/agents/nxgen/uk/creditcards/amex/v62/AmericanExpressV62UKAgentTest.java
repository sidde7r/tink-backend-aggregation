package se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class AmericanExpressV62UKAgentTest {

    private final String USERNAME = "";
    private final String PASSWORD = "";

    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-americanexpress-password")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}
