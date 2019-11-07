package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.revolut;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class RevolutAgentTest {
    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-revolut-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("revolut")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
