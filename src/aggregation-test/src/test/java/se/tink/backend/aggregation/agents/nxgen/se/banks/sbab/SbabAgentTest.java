package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class SbabAgentTest {
    private final String TEST_USERNAME = "YYYYMMDD";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup(){
        builder = new AgentIntegrationTest.Builder("se", "se-sbab_sandbox-oauth2")
                .addCredentialField(Field.Key.USERNAME, TEST_USERNAME)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception{
        builder.build().testRefresh();
    }
}
