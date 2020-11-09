package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.agent;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;

public class SodexoAgentTest {

    private final ArgumentManager<UsernamePasswordArgumentEnum> manager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("pt", "pt-sodexo-password")
                        .addCredentialField(
                                Field.Key.USERNAME,
                                manager.get(UsernamePasswordArgumentEnum.USERNAME))
                        .addCredentialField(
                                Field.Key.PASSWORD,
                                manager.get(UsernamePasswordArgumentEnum.PASSWORD))
                        .setFinancialInstitutionId("1106978b385144189093bd9455d2080d")
                        .expectLoggedIn(false)
                        .saveCredentialsAfter(true)
                        .loadCredentialsBefore(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
