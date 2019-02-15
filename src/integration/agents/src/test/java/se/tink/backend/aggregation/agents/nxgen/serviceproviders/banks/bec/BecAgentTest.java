package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import static org.junit.Assert.*;

@Ignore
public class BecAgentTest {

    private final String TEST_USERNAME = "";
    private final String TEST_PASSWORD = "";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup(){
        builder = new AgentIntegrationTest.Builder("dk", "dk-nykredit-password")
                .addCredentialField(Field.Key.USERNAME, TEST_USERNAME)
                .addCredentialField(Field.Key.PASSWORD, TEST_PASSWORD)
                .loadCredentialsBefore(false)
                .expectLoggedIn(false)
                .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception{

        builder.build().testRefresh();
    }

}