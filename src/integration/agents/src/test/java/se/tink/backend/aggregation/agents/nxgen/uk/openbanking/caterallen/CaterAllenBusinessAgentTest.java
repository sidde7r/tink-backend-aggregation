package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.caterallen;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class CaterAllenBusinessAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-caterallen-business-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("2345494d2df5400a9118dcbed7d0cb9e")
                .build()
                .testRefresh();
    }
}
