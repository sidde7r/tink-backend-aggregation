package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class SwedbankSEAgentTest {
    private final ArgumentManager<SsnArgumentEnum> manager =
            new ArgumentManager<>(SsnArgumentEnum.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRegisterAndRefresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("se", "swedbank-bankid")
                        .addCredentialField(Field.Key.USERNAME, manager.get(SsnArgumentEnum.SSN))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA);

        builder.build().testRefresh();
    }
}
