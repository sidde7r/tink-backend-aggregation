package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.natwestgroup.couttsandco;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class CouttsAndCoAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-couttsandco-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("92569b534cfc41e3a80deaf106b508e1")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
