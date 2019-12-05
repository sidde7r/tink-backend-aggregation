package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.agent;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.credentials.service.RefreshableItem;

@Ignore
public class BancoBpiAgentTest {

    private enum Arg {
        USERNAME,
        PASSWORD
    }

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
                new AgentIntegrationTest.Builder("pt", "pt-bancobpi-password")
                        /*.addCredentialField(
                            Field.Key.USERNAME, manager.get(Arg.USERNAME))
                        .addCredentialField(
                            Field.Key.PASSWORD, manager.get(Arg.PASSWORD))*/
                        .expectLoggedIn(false)
                        .saveCredentialsAfter(false)
                        .loadCredentialsBefore(true)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
