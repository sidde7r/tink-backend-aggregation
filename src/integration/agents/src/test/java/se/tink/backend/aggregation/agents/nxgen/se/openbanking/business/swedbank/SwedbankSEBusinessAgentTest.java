package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.swedbank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;

public class SwedbankSEBusinessAgentTest {

    private AgentIntegrationTest.Builder builder;
    private final ArgumentManager<SsnArgumentEnum> ssnManager =
            new ArgumentManager<>(SsnArgumentEnum.values());
    private final ArgumentManager<UsernameArgumentEnum> psuManager =
            new ArgumentManager<>(UsernameArgumentEnum.values());

    @Before
    public void setup() {
        ssnManager.before();
        psuManager.before();
    }

    private AgentIntegrationTest setupAgentTest(final String providerName) {
        return new AgentIntegrationTest.Builder("SE", providerName)
                .addCredentialField(Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                .addCredentialField(Key.CORPORATE_ID, psuManager.get(UsernameArgumentEnum.USERNAME))
                .expectLoggedIn(false)
                .setFinancialInstitutionId("swedbank")
                .setAppId("tink")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build();
    }

    @Test
    public void testSwedbankRefresh() throws Exception {
        setupAgentTest("se-swedbank-business-ob").testRefresh();
    }

    @Test
    public void testSavingsbankRefresh() throws Exception {
        setupAgentTest("se-savingsbank-business-ob").testRefresh();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
