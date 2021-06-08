package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.firsttrust;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class FirstTrustCorporateAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-firsttrust-corporate-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("793c6ee3633a4d5d8e0f5bad26f3bb50")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
