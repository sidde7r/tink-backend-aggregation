package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class CaisseEpargneAgentTest {

    private final String USERNAME = ""; // 10 digits
    private final String PASSWORD = ""; // 8 digits

    @Test
    public void testLoginRefresh() throws Exception {

        new AgentIntegrationTest.Builder(CaisseEpargneConstants.MARKET, CaisseEpargneConstants.PROVIDER_NAME)
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }

}
