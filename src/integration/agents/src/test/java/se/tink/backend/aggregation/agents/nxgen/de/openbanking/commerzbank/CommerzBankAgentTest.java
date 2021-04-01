package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;

public class CommerzBankAgentTest {
    private AgentIntegrationTest.Builder builder;

    private final ArgumentManager<ArgumentManager.UsernameArgumentEnum> manager =
            new ArgumentManager<>(UsernameArgumentEnum.values());;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("de", "de-commerzbank-ob")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .addCredentialField(
                                Key.USERNAME, manager.get(UsernameArgumentEnum.USERNAME))
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("commerzbank")
                        .setAppId("tink");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
