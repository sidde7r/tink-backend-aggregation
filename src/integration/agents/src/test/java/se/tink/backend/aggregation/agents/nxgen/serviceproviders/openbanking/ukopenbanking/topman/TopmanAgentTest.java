package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.topman;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class TopmanAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-topman-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("6f0c298acf8c4c3f818beed28de326b6")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
