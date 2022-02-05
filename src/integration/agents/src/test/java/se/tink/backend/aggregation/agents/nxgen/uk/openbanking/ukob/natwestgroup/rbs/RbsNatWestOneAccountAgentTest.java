package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.natwestgroup.rbs;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class RbsNatWestOneAccountAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-rbs-natwestoneaccount-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("ed47ff3492924bf5855df4d780cdffdc")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
