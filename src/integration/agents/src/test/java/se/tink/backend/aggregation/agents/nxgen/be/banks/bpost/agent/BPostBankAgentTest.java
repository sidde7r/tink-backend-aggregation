package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.agent;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.credentials.service.RefreshableItem;

public class BPostBankAgentTest {
    private final ArgumentManager<ArgumentManager.UsernameArgumentEnum> manager =
            new ArgumentManager<>(ArgumentManager.UsernameArgumentEnum.values());
    private final ArgumentManager<ArgumentManager.UserDataArgumentEnum> managerUserData =
            new ArgumentManager<>(ArgumentManager.UserDataArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        managerUserData.before();
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("be", "be-bpost-cardreader")
                        .addCredentialField(
                                Field.Key.EMAIL,
                                managerUserData.get(ArgumentManager.UserDataArgumentEnum.EMAIL))
                        .addCredentialField(
                                Field.Key.USERNAME,
                                manager.get(ArgumentManager.UsernameArgumentEnum.USERNAME))
                        .expectLoggedIn(false)
                        .saveCredentialsAfter(true)
                        .loadCredentialsBefore(true)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
