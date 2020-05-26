package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.CredentialKeys;

public class EnterCardAgentTest {
    private final ArgumentManager<SsnArgumentEnum> manager =
            new ArgumentManager<>(SsnArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @Before
    public void before() {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    public AgentIntegrationTest createTestAgent(final String providerName) {
        return new AgentIntegrationTest.Builder("se", providerName)
                .addCredentialField(CredentialKeys.SSN, manager.get(SsnArgumentEnum.SSN))
                .setFinancialInstitutionId("entercard")
                .setAppId("tink")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .build();
    }

    @Test
    public void testCoop() throws Exception {
        createTestAgent("se-coop-ob").testRefresh();
    }

    @Test
    public void testRemember() throws Exception {
        createTestAgent("se-remembermastercard-ob").testRefresh();
    }

    @Test
    public void testMoreGolf() throws Exception {
        createTestAgent("se-moregolfmastercard-ob").testRefresh();
    }

    @Test
    public void testMervarde() throws Exception {
        createTestAgent("se-mervardemastercard-ob").testRefresh();
    }
}
