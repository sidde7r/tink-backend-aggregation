package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenConstants.CredentialKeys;

@Ignore
public class RaiffeisenAgentTest {
    private final String TEST_IBAN = "HU19120010080010059400100008";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("hu", "hu-raiffeisen-oauth2")
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
