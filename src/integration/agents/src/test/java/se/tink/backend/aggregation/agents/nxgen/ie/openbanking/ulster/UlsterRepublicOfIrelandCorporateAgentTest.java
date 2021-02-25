package se.tink.backend.aggregation.agents.nxgen.ie.openbanking.ulster;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class UlsterRepublicOfIrelandCorporateAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("ie", "ie-ulster-corporate-ob")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("759222c21f6a4b88a7a7191376b81d43")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
