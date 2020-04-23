package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.danskebank;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class DanskebankBusinessSEAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("se", "se-danskebank-business-ob")
                .setAppId("tink")
                .setFinancialInstitutionId("danskebank")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
