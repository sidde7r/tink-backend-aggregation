package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class OpenbankAgentTest {
    private final String TEST_USERNAME = "nnnnnnnnY";
    private final String TEST_PASSWORD = "nnnn";
    private final String TEST_USERNAME_TYPE = "N";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup(){
        builder = new AgentIntegrationTest.Builder("es", "es-openbank-password")
                .addCredentialField(Field.Key.USERNAME, TEST_USERNAME)
                .addCredentialField(OpenbankConstants.USERNAME_TYPE, TEST_USERNAME_TYPE)
                .addCredentialField(Field.Key.PASSWORD, TEST_PASSWORD)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception{
        builder.build().testRefresh();
    }
}
