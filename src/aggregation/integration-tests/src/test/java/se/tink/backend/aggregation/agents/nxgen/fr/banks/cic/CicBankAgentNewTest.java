package se.tink.backend.aggregation.agents.nxgen.fr.banks.cic;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class CicBankAgentNewTest {

    private static final String USERNAME = "<username>";
    private static final String PASSWORD = "<password>";

    private static AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder("fr", "fr-cic-password")
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
