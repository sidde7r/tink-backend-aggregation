package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.markandspencer;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class MarkAndSpencerAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-markandspencer-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("47d97a96282842daa8c66ad79028f4af")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
