package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class OpBankAgentTest {

    private final String TEST_USERNAME = "SOMEUSER";
    private final String TEST_PASSWORD = "SOMEPASS";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup(){
        builder = new AgentIntegrationTest.Builder("fi", "fi-opbank-openbanking")
                .addCredentialField(Field.Key.USERNAME, TEST_USERNAME)
                .addCredentialField(Field.Key.PASSWORD, TEST_PASSWORD)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception{
        builder.build().testRefresh();
    }
}
