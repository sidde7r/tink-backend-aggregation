package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.bip;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class BipAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-bip-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("941a7515709f45c898834db2f2332e0b")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
