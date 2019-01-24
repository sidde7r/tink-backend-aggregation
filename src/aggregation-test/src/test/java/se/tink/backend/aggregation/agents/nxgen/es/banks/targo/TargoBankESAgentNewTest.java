package se.tink.backend.aggregation.agents.nxgen.es.banks.targo;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class TargoBankESAgentNewTest {

    public static final String USERNAME = "<username>";
    public static final String PASSWORD = "<password>";

    private static AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder("es", "es-targobank-password")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false);
    }

    @Test
    public void testTargoBankEs() throws Exception {
        builder()
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .build()
                .testRefresh();
    }
}
