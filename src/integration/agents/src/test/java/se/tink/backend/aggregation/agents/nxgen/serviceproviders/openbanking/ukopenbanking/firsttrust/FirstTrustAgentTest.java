package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.firsttrust;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class FirstTrustAgentTest {
    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-firsttrust-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("firsttrust")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
