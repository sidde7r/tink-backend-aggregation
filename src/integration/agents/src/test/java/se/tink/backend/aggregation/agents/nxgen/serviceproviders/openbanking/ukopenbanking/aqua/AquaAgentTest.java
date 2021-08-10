package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.aqua;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class AquaAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-aqua-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("5ccb78f488894a04a737eb3dea95eda0")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
