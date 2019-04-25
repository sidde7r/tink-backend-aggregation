package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.CredentialKeys;

@Ignore
public class DkbAgentTest {

    private final String TEST_USERNAME = "curakec";
    private final String TEST_PASSWORD = "Curakec_1";
    private final String TEST_IBAN = "FR7612345987650123456789014";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("DE", "de-dkb-password")
                        .addCredentialField(Field.Key.USERNAME, TEST_USERNAME)
                        .addCredentialField(Field.Key.PASSWORD, TEST_PASSWORD)
                        .addCredentialField(CredentialKeys.IBAN, TEST_IBAN)
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
