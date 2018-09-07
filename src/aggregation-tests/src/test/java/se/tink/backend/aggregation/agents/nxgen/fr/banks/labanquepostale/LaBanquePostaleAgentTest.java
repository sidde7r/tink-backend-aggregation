package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class LaBanquePostaleAgentTest {

    private final String TEST_USERNAME = "NNNN";
    private final String TEST_PASSWORD = "NNNN";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup(){
        builder = new AgentIntegrationTest.Builder("fr", "fr-labanquepostale-password")
                .addCredentialField(Field.Key.USERNAME, TEST_USERNAME)
                .addCredentialField(Field.Key.PASSWORD, TEST_PASSWORD)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {

        builder.build().testRefresh();
    }
}
