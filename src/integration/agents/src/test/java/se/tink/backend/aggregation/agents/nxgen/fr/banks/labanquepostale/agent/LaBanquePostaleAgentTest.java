package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.agent;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.LaBanquePostaleConstants;

public class LaBanquePostaleAgentTest {

    private static final String TEST_USERNAME = "";
    private static final String TEST_PASSWORD = "";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder(
                                LaBanquePostaleConstants.MARKET,
                                LaBanquePostaleConstants.PROVIDER_NAME)
                        .addCredentialField(Field.Key.USERNAME, TEST_USERNAME)
                        .addCredentialField(Field.Key.PASSWORD, TEST_PASSWORD)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .transactionsToPrint(99999);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
