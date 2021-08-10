package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.wallis;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class WallisAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-wallis-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("baa7d6234e224d1198458593164b8158")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
