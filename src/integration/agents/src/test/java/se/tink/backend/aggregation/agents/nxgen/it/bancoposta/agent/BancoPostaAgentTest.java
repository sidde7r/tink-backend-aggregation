package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.agent;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;

public class BancoPostaAgentTest {

    private enum Arg implements ArgumentManagerEnum {
        USERNAME,
        PASSWORD,
        ACCESS_PIN;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<BancoPostaAgentTest.Arg> manager =
            new ArgumentManager<>(BancoPostaAgentTest.Arg.values());

    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("it", "it-bancoposta-password")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                        .addCredentialField(Field.Key.PASSWORD, manager.get(Arg.PASSWORD))
                        .addCredentialField(Field.Key.ACCESS_PIN, manager.get(Arg.ACCESS_PIN))
                        .expectLoggedIn(false)
                        .saveCredentialsAfter(true)
                        .loadCredentialsBefore(false)
                        .setFinancialInstitutionId("bancoposta")
                        .setAppId("tink");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
