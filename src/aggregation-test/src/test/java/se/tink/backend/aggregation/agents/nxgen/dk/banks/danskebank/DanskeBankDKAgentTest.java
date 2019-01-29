package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class DanskeBankDKAgentTest {
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setUp() throws Exception {
        builder = new AgentIntegrationTest.Builder("dk", "dk-danskebank-servicecode")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build()
                .testRefresh();
    }
}
