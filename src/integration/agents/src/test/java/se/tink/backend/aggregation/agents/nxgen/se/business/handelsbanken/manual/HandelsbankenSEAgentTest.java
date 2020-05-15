package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class HandelsbankenSEAgentTest {

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
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("se", "handelsbanken-business-bankid")
                .addCredentialField(Field.Key.USERNAME, manager.get(SsnArgumentEnum.SSN))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .addRefreshableItems(RefreshableItem.CHECKING_ACCOUNTS)
                .addRefreshableItems(RefreshableItem.CHECKING_TRANSACTIONS)
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }
}
