package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;


@Ignore
public class VolksbankAgentTest {
    @Test
    public void testLoginAndRefresh() throws Exception {
        new AgentIntegrationTest.Builder("at", "at-volksbank-password")
                .addCredentialField(VolksbankConstants.CREDENTIAL_USERNUMBER, "")
                .addCredentialField(Field.Key.USERNAME, "")
                .addCredentialField(Field.Key.PASSWORD, "")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }
}
