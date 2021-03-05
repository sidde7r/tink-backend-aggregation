package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.bankofireland;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class BankOfIrelandAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-bankofireland-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("60808d9d276b4e44b8a7b05a601262a1")
                .build()
                .testRefresh();
    }
}
