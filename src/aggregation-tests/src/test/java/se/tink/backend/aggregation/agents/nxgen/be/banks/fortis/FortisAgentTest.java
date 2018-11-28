package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class FortisAgentTest {

    private static final String USERNAME = "";
    private static final String PASSWORD = "";
    private static final String CLIENTNUMBER = "";

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("be", "be-fortis-cardreader")
                    .addCredentialField(Field.Key.USERNAME, USERNAME)
                    .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                    .addCredentialField("clientnumber", CLIENTNUMBER)
                    .loadCredentialsBefore(true)
                    .expectLoggedIn(false)
                    .saveCredentialsAfter(false);

    @Test
    public void testRefresh() throws Exception {
        builder.build()
                .testRefresh();
    }

}

