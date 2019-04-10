package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class N26AgentTest {

    public static final String USERNAME = "";
    public static final String PASSWORD = "";

    @Test
    public void refresh() throws Exception {
        new AgentIntegrationTest.Builder("de", "de-n26-password")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}
