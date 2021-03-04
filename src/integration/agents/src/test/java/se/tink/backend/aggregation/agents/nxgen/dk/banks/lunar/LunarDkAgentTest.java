package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.credentials.service.RefreshableItem;

public class LunarDkAgentTest {

    private final ArgumentManager<ArgumentManager.UsernamePasswordArgumentEnum> manager =
            new ArgumentManager<>(ArgumentManager.UsernamePasswordArgumentEnum.values());

    @Before
    public void before() {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testLoginAndRefresh() throws Exception {
        new AgentIntegrationTest.Builder("dk", "dk-lunar-nemid")
                .addCredentialField(
                        Field.Key.USERNAME,
                        manager.get(ArgumentManager.UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD,
                        manager.get(ArgumentManager.UsernamePasswordArgumentEnum.PASSWORD))
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .expectLoggedIn(false)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }
}
