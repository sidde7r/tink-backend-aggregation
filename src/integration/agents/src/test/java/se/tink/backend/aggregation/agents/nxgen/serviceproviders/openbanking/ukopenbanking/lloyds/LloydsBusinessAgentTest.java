package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.lloyds;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class LloydsBusinessAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-lloyds-business-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("16343e744a874337b11efdd7cbd25a53")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
