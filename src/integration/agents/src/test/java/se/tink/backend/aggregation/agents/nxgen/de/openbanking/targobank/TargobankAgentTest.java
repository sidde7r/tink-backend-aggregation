package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.PasswordArgumentEnum;

public class TargobankAgentTest {
    private final ArgumentManager<PasswordArgumentEnum> manager =
            new ArgumentManager<>(PasswordArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("de", "de-targobank-ob")
                        .addCredentialField(
                                Field.Key.PASSWORD,
                                manager.get(ArgumentManager.PasswordArgumentEnum.PASSWORD))
                        .loadCredentialsBefore(false)
                        .expectLoggedIn(false)
                        .saveCredentialsAfter(false)
                        .setAppId("tink")
                        .setFinancialInstitutionId("targobank");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    @AfterClass
    public static void cleanup() {
        ArgumentManager.afterClass();
    }
}
