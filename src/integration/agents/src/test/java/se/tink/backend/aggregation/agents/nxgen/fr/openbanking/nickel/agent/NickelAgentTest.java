package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.agent;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class NickelAgentTest {

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("fr", "fr-nickel-ob")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                        .addCredentialField(Field.Key.PASSWORD, manager.get(Arg.PASSWORD))
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("94b3a4f4f6cf4f26b8051921b3a8c0f3")
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .setAppId("tink");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    private enum Arg implements ArgumentManagerEnum {
        USERNAME,
        PASSWORD;

        @Override
        public boolean isOptional() {
            return false;
        }
    }
}
