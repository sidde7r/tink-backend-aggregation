package se.tink.backend.aggregation.agents.nxgen.at.banks.ErsteBank;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class ErsteBankAgentTest {

    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    @Test
    public void testLoginAndRefresh() throws Exception {
        new AgentIntegrationTest.Builder("at", "at-erstebank-password")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }


    @Test
    public void testLoginAndRefreshSidentity() throws Exception {
        new AgentIntegrationTest.Builder("at", "at-erstebank-thirdparty")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}

