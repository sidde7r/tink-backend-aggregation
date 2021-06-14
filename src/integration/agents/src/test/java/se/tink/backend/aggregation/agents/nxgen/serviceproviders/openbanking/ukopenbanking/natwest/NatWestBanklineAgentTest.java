package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.natwest;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class NatWestBanklineAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-natwest-bankline-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("2ad9cb1542c54c098653a73d7b75c11c")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
