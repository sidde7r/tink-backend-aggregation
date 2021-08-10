package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.opus;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class OpusAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-opus-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("6082db20dd954d4baab77dd760040c5e")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
