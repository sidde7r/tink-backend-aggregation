package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.virginmoney;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class VirginMoneyBusinessAgentTest {
    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-virginmoney-business-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("a56a4a134088494ebd1d9188bb2e6a7d")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
