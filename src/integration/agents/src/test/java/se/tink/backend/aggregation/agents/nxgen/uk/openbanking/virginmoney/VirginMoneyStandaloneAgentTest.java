package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.virginmoney;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class VirginMoneyAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-virginmoney-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("a56a4a134088494ebd1d9188bb2e6a7d")
                .build()
                .testRefresh();
    }
}
