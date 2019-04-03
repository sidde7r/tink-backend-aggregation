package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class RaiffeisenAgentTest {

    private final String TEST_IBAN = "AT099900000000001511";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("at", "at-raiffeisen-oauth2")
                        .addCredentialField(RaiffeisenConstants.CredentialKeys.IBAN, TEST_IBAN)
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
