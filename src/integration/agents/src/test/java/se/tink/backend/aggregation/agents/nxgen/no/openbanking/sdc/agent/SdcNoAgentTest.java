package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sdc.agent;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class SdcNoAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("no", "no-sogn-sparebank-ob")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false)
                        .setAppId("tink")
                        .setFinancialInstitutionId("banknordik");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
