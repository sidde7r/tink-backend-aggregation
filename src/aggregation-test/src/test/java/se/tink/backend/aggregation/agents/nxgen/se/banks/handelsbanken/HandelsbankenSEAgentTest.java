package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class HandelsbankenSEAgentTest {
    private final String USERNAME = ""; // 12 digit SSN

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("se", "handelsbanken-bankid")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}
