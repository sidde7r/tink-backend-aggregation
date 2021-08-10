package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.missselfridge;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class MissSelfridgeAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-miss-selfridge-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("ce795f76fbb3489db07060c2568dbc5a")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
