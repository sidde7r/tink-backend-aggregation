package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ulster;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class UlsterUniversalAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-ulster-universal-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("0c7ba941addb428c83d6ea554ecace56")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
