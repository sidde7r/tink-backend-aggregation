package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.hsbc;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class HsbcBusinessAgentTest {

    private static final String HSBC_FINANCIAL_INSTITUTION_ID = "6cd6d369ba8e4d72b1a7d26dabe509a3";

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-hsbc-business-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId(HSBC_FINANCIAL_INSTITUTION_ID)
                .build()
                .testRefresh();
    }
}
