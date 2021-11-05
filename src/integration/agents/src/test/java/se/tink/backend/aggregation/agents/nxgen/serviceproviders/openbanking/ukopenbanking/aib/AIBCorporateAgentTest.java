package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.aib;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class AIBCorporateAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-aib-business-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("eab9d48aca91445e9fbf2523564f4577")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
