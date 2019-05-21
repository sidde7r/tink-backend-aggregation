package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.credentials.service.RefreshableItem;

public class HandelsbankenFIAgentTest {

    private enum Arg {
        USERNAME,
        PIN,
        SIGNUP_PASSWORD
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

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
        new AgentIntegrationTest.Builder("fi", "fi-handelsbanken-codecard")
                .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                .addCredentialField(Field.Key.PASSWORD, manager.get(Arg.PIN))
                .addCredentialField(
                        HandelsbankenFIConstants.DeviceAuthentication.SIGNUP_PASSWORD,
                        manager.get(Arg.SIGNUP_PASSWORD))
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }
}
