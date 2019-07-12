package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.FieldKeys;

@Ignore
public class FidorAgentTest {

    private final String TEST_USERNAME = "s.jankovic@vegaitsourcing.rs";
    private final String TEST_PASSWORD = "password123";
    private final String IBAN_TEST = "DE71100100100060069725";
    private final String BBAN_TEST = "0009536830";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("de", "de-fidor-password")
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
