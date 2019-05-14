package se.tink.backend.aggregation.agents.brokers.nordnet;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.credentials.service.RefreshableItem;

public class NordnetAgentTest {

    private enum Arg {
        USERNAME
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("se", "nordnet-bankid")
                .addCredentialField(Key.USERNAME, manager.get(Arg.USERNAME))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }

    @AfterClass
    public static void afterClass() {

        ArgumentManager.afterClass();
    }
}
