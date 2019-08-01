package se.tink.backend.aggregation.agents.nxgen.se.banks.seb;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.credentials.service.RefreshableItem;

public class SEBAgentTest {

    private enum Arg {
        SSN,
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    private AgentIntegrationTest.Builder testBuilder;

    @Before
    public void before() {
        manager.before();

        testBuilder =
                new AgentIntegrationTest.Builder("se", "se-seb-bankid")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.SSN))
                        .expectLoggedIn(false)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testLoginAndRefresh() throws Exception {
        testBuilder.build().testRefresh();
    }
}
