package se.tink.backend.aggregation.agents.nxgen.de.banks.targo;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class TargoBankDEAgentNewTest {

    public static final String PASSWORD = "<password>";
    public static final String USERNAME = "<username>";

    private static AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder("de", "targobank-password")
                .setCredentialType(CredentialsTypes.PASSWORD)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false);
    }

    @Test
    public void testTargoBankDe() throws Exception {
        builder()
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .build()
                .testRefresh();
    }
}
