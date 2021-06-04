package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class HsbcV31NetAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-hsbc-net-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("6cedf51d0bac48439b055ab2caede3cb")
                .build()
                .testRefresh();
    }
}
