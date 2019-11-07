package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.firstdirect;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class FirstDirectAgentTest {
    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-firstdirect-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("firstdirect")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
