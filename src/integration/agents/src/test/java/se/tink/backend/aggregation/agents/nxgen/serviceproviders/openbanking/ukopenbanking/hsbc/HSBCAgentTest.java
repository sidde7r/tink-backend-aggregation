package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.hsbc;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class HSBCAgentTest {
    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-hsbc-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("hsbc")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
