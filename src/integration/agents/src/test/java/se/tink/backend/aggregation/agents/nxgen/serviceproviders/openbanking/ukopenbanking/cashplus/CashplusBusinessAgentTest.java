package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.cashplus;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class CashplusBusinessAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-cashplus-business-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("71af0cf64e9a42caba81a4aaaa423dc8")
                .build()
                .testRefresh();
    }
}
