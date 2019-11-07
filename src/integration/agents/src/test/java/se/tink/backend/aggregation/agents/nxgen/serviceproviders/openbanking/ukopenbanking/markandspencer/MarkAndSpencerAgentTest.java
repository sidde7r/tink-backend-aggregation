package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.markandspencer;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class MarkAndSpencerAgentTest {
    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-markandspencer-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("markandspencer")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
