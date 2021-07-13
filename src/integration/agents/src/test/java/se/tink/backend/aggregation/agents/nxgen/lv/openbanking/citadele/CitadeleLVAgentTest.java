package se.tink.backend.aggregation.agents.nxgen.lv.openbanking.citadele;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class CitadeleLVAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("LV", "lv-citadele-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("citadele")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
