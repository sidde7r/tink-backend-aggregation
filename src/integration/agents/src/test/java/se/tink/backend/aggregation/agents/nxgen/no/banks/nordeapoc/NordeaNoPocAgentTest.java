package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class NordeaNoPocAgentTest {

    @Test
    public void testRefreshBankId() throws Exception {
        new AgentIntegrationTest.Builder("no", "no-nordea-bankid-poc")
                .setAppId("tink")
                .setFinancialInstitutionId("nordea-bankid")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(true)
                .build()
                .testRefresh();
    }
}
