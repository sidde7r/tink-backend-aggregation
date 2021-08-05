package se.tink.backend.aggregation.agents.nxgen.it.openbanking.deutschebank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.CredentialKeys;

public class DeutscheBankITAgentTest {

    private final ArgumentManager<UsernameArgumentEnum> usernameManager =
            new ArgumentManager<>(UsernameArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        usernameManager.before();
        builder =
                new AgentIntegrationTest.Builder("it", "it-deutschebank-ob")
                        .addCredentialField(
                                CredentialKeys.USERNAME,
                                usernameManager.get(UsernameArgumentEnum.USERNAME))
                        .setFinancialInstitutionId("deutschebank-it")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .expectLoggedIn(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
