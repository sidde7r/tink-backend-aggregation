package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.siliconvalley;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class SiliconValleyBusinessAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-siliconvalley-business-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("bda10c7cd3a94adda16a986b29c3f8fb")
                .build()
                .testRefresh();
    }
}
