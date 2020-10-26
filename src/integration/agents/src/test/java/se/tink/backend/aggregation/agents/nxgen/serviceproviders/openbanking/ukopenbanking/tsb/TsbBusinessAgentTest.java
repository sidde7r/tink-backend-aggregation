package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.tsb;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class TsbBusinessAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-tsb-business-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("a4667f95a9c4464eb505dd5e3543d41e")
                .build()
                .testRefresh();
    }
}
