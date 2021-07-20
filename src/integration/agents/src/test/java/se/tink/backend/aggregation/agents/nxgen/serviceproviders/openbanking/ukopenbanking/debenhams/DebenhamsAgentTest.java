package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.debenhams;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class DebenhamsAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-debenhams-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("bdda0dbc38e749f298f30acb224b2d28")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
