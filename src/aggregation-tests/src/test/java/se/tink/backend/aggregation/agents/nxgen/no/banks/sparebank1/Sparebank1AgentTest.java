package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class Sparebank1AgentTest {
    private final String USERNAME = "";
    private final String MOBILE_NUMBER = "";

    @Test
    public void testRegister() throws Exception {
        new AgentIntegrationTest.Builder("no", "no-sparebank1-nord-norge")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.MOBILENUMBER, MOBILE_NUMBER)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }

    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder("no", "no-sparebank1-nord-norge")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.MOBILENUMBER, MOBILE_NUMBER)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }
}
