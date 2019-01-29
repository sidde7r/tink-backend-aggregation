package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class SjPrioMastercardAgentTest {
    private final String USERNAME = "YYYYMMDDNNNN"; // 12 digit SSN

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("se", "sjpriomastercard-bankid")
                        .addCredentialField(Field.Key.USERNAME, USERNAME)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .doLogout(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
