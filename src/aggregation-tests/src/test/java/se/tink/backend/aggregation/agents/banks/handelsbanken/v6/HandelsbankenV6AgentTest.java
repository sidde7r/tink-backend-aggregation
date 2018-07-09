package se.tink.backend.aggregation.agents.banks.handelsbanken.v6;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class HandelsbankenV6AgentTest {

    @Test
    public void name() throws Exception {
        new AgentIntegrationTest.Builder("se", "handelsbanken-bankid")
                .addCredentialField(Field.Key.USERNAME, "YYYYMMDDNNNN")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}
