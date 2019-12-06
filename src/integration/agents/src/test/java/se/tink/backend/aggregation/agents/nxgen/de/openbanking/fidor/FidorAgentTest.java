package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.FieldKeys;

@Ignore
public class FidorAgentTest {

    private final String TEST_USERNAME = "[redacted]";
    private final String TEST_PASSWORD = "[redacted]";
    private final String IBAN_TEST = "[redacted]";
    private final String BBAN_TEST = "[redacted]";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("de", "de-fidor-ob")
                        .addCredentialField(Field.Key.USERNAME, TEST_USERNAME)
                        .addCredentialField(Field.Key.PASSWORD, TEST_PASSWORD)
                        .addCredentialField(FieldKeys.IBAN, IBAN_TEST)
                        .addCredentialField(FieldKeys.BBAN, BBAN_TEST)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
