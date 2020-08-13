package se.tink.backend.aggregation.agents.nxgen.fr.banks.hellobank.manual;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class HelloBankAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fr", "fr-hellobank-password")
                        .addCredentialField(Field.Key.USERNAME, "")
                        .addCredentialField(Field.Key.PASSWORD, "")
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
