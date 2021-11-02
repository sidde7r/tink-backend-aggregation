package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.arcadia;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class ArcadiaAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-arcadia-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("39b1d8734957460bafccc08e354e4c5b")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
