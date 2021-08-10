package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fluid;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class FluidAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-fluid-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("15ca1c6e1d7c48d79880a008a0174ba9")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
