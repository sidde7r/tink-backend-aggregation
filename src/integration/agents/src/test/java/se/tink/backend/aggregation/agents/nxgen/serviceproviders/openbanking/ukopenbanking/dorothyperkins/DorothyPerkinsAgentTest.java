package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.dorothyperkins;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class DorothyPerkinsAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-dorothy-perkins-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("0b4ceffd3f8a436b9247f0e810ffe896")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
