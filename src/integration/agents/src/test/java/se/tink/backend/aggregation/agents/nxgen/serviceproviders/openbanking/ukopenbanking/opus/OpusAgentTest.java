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
                .setFinancialInstitutionId("bdda0dbc38e749f298f30acb224b2d28")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
