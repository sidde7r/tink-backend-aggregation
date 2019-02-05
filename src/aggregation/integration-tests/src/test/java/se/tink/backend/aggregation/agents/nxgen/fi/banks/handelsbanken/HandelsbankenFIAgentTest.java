package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class HandelsbankenFIAgentTest {
    private final String USERNAME = "";
    private final String PIN = "";
    private final String SIGNUP_PASSWORD = "";

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("fi", "fi-handelsbanken-codecard")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PIN)
                .addCredentialField(HandelsbankenFIConstants.DeviceAuthentication.SIGNUP_PASSWORD, SIGNUP_PASSWORD)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }
}
