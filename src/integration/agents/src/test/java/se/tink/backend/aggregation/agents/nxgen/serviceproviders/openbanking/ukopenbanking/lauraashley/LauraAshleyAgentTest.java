package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.lauraashley;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class LauraAshleyAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-laura-ashley-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("1590e02c81a840579ebc89b33734a1b8")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
