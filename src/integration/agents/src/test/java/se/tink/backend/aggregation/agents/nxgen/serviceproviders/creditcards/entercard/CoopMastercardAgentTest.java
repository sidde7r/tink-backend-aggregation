package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class CoopMastercardAgentTest {

    private final String USERNAME = "YYYYMMDDNNNN"; // 12 digit SSN

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("se", "coopmastercard-bankid")
                        .addCredentialField(Field.Key.USERNAME, USERNAME)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
