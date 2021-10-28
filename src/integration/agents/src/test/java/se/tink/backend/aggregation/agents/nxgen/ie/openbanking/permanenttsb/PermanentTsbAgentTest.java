package se.tink.backend.aggregation.agents.nxgen.ie.openbanking.permanenttsb;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class PermanentTsbAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("ie", "ie-permanent-tsb-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("7badc7034e42422593e58b6d0db05e4d")
                .build()
                .testRefresh();
    }
}
