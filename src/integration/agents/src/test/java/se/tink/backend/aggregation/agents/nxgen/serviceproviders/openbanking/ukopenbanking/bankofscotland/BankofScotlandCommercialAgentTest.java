package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.bankofscotland;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class BankofScotlandCommercialAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-bankofscotland-commercial-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("d660f95b315a49d493952778ee23b509")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
