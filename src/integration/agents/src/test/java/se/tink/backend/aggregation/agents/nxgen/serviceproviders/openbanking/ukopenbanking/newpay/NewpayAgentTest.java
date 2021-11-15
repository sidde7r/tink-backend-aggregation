package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.newpay;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class NewpayAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-newpay-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("ccbb92d5b49a454c889a55ca16fa71b1")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
