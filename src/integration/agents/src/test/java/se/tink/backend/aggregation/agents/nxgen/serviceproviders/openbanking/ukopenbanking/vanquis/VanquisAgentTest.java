package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.vanquis;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class VanquisAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-vanquis-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("19bba3efcf134006a14f9f0c957508fc")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
