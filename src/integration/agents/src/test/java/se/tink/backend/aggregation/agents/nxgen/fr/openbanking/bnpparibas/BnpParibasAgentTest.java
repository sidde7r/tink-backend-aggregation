package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bnpparibas;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class BnpParibasAgentTest {
    private final String TEST_USERNAME = "";
    private final String TEST_PASSWORD = "";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fr", "fr-bnpparibas-password")
                        .addCredentialField(Field.Key.USERNAME, TEST_USERNAME)
                        .addCredentialField(Field.Key.PASSWORD, TEST_PASSWORD)
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
