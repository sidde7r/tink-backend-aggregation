package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.tide;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class TideBusinessAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-tide-business-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("13d7f079d69747e3a40f04ab1bc82199")
                .build()
                .testRefresh();
    }
}
