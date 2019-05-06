package se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.CrelanConstants.CredentialKeys;

@Ignore
public class CrelanAgentTest {
    public final String TEST_IBAN = "BE87548648412194";
    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("be", "be-crelan-oauth2")
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
