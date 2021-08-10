package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.argos;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class ArgosAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-argos-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("0e337f41d1c24f63bdbb3d7718b9ddc5")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
