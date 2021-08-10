package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.burtonmenswear;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class BurtonMenswearAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-burton-menswear-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("2baa56696730497caa0b42acab04c5fb")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
