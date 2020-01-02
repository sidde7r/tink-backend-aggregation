package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.CredentialKeys;

public abstract class EnterCardBaseAgentTest {
    private final ArgumentManager<UsernameArgumentEnum> manager =
            new ArgumentManager<>(UsernameArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    private String providerName;

    public EnterCardBaseAgentTest(String providerName) {
        this.providerName = providerName;
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("se", providerName)
                        .addCredentialField(
                                CredentialKeys.SSN, manager.get(UsernameArgumentEnum.USERNAME))
                        .setFinancialInstitutionId("entercard")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
