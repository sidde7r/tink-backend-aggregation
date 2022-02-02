package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.bankofireland;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class BankOfIrelandBusinessAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-bankofireland-business-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("60808d9d276b4e44b8a7b05a601262a1")
                .build()
                .testRefresh();
    }
}
