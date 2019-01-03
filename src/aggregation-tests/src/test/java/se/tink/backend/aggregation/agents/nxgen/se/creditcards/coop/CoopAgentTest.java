package se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

public class CoopAgentTest {

    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("se", "coop")
                        .addCredentialField(Field.Key.USERNAME, USERNAME)
                        .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true)
                        .doLogout(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
        builder.loadCredentialsBefore(true).build().testRefresh();
    }

}