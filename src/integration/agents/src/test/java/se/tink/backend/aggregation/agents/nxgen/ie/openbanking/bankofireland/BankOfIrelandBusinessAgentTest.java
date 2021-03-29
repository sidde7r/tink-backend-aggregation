package se.tink.backend.aggregation.agents.nxgen.ie.openbanking.bankofireland;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class BankOfIrelandBusinessAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("ie", "ie-bankofireland-business-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("186d4184efcb4e78bf8bfad08978c133")
                .build()
                .testRefresh();
    }
}
