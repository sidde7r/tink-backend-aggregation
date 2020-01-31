package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.agents;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class BpDuNordAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fr", "fr-bpdunord-ob")
                        .setFinancialInstitutionId("bpdunord")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
