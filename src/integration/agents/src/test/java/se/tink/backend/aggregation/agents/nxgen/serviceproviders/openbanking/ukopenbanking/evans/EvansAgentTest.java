package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.evans;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class EvansAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-evans-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("36d102ac47f54007b035c0c516ed655e")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
