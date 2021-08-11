package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.agent;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class SdcAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("no", "no-sogn-sparebank-ob")
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false)
                        .setAppId("tink")
                        .setFinancialInstitutionId("2248e24e66954822b36056d9413e8fdf");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
