package se.tink.backend.aggregation.agents.nxgen.be.banks.beobank;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class BeobankAgentTest {

    private static final String USERNAME = "<username>";
    private static final String PASSWORD = "<password>";

    private static AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder("be", "be-beobank-password")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false);
    }

    @Test
    public void testCicbankFr() throws Exception {
        builder()
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .build()
                .testRefresh();
    }
}
