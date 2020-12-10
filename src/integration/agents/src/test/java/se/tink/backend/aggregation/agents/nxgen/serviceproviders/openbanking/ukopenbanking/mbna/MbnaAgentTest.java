package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.mbna;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class MbnaAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-mbna-ob")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("mbna")
                .build()
                .testRefresh();
    }
}
