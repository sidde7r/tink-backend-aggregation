package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ulster;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class UlsterCorporateAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-ulster-corporate-ob")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("ulster")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
