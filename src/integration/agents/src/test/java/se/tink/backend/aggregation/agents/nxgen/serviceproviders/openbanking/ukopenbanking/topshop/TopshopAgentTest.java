package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.topshop;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class TopshopAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-topshop-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("68b18277682642879eeb328bc17a04d6")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
