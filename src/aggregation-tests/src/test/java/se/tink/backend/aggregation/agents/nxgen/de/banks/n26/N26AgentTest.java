package se.tink.backend.aggregation.agents.nxgen.de.banks.n26;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

public class N26AgentTest {

    public static final String USERNAME = "";
    public static final String PASSWORD = "";

    @Test
    public void refresh() throws Exception {
        new AgentIntegrationTest.Builder("de", "de-n26-password")
                .setCredentialType(CredentialsTypes.PASSWORD)
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();

    }

}
