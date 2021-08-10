package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.marbles;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class MarblesAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-marbles-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("6eeb5a88c48542c6accbec64374a3210")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
