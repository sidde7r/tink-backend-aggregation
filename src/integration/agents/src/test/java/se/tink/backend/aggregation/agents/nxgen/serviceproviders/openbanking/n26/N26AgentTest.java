package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class N26AgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("fr", "fr-n26-ob")
                .setAppId("tink")
                .setFinancialInstitutionId("n26")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
