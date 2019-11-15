package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.credentials.service.RefreshableItem;

public class CaixaAgentTest {

    private enum Arg {
        USERNAME,
        PASSWORD
    }

    private final ArgumentManager<CaixaAgentTest.Arg> manager =
            new ArgumentManager<>(CaixaAgentTest.Arg.values());

    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("pt", "pt-caixa-password")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                        .addCredentialField(Field.Key.PASSWORD, manager.get(Arg.PASSWORD))
                        .expectLoggedIn(false)
                        .saveCredentialsAfter(false)
                        .loadCredentialsBefore(false)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray());
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
