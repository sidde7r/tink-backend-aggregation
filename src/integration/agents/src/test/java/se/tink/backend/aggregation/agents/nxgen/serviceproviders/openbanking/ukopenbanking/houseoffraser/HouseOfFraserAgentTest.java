package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.houseoffraser;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class HouseOfFraserAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-house-of-fraser-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("d72caa80c46d4047a3f0346288343abb")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
