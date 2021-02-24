package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.bankofireland;

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
                .setFinancialInstitutionId("20db5a707c02460eb9a6eba771f80f45")
                .build()
                .testRefresh();
    }
}
