package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;

public class CommerzBankAgentTest {
    private AgentIntegrationTest.Builder builder;

    private enum Arg implements ArgumentManagerEnum {
        USERNAME;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

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
                        .addCredentialField(Key.USERNAME, manager.get(Arg.USERNAME))
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("commerzbank")
                        .setAppId("tink");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
