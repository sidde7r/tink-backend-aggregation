package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class SBABAgentTest {

    private final String TEST_USERNAME = "SOMEUSER";
    private final String TEST_PASSWORD = "SOMEPASS";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup(){
        builder = new AgentIntegrationTest.Builder("se", "se-sbab-oauth2")
                .addCredentialField(Field.Key.USERNAME, TEST_USERNAME)
                .addCredentialField(Field.Key.PASSWORD, TEST_PASSWORD)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false);

    }

    @Test
    public void testRefresh() throws Exception{
        builder.build().testRefresh();
    }
}
