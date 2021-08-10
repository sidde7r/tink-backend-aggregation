package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc.agent;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class SdcDkAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("dk", "dk-sparekassenkronjylland-ob")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false)
                        .setAppId("tink")
                        .setFinancialInstitutionId("85525e462d4e46629f6f4ccf9698d618");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
