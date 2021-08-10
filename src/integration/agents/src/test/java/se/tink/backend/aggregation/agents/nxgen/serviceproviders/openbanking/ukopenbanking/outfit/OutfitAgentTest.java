package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.outfit;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class OutfitAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-outfit-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("0d7827f026e04141894cd98653ff1f80")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
