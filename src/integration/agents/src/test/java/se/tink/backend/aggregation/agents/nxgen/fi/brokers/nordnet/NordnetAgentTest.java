package se.tink.backend.aggregation.agents.nxgen.fi.brokers.nordnet;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.credentials.service.RefreshableItem;

public class NordnetAgentTest {
    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        USERNAME,
        PASSWORD;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    private AgentIntegrationTest.Builder bankIdBuilder;
    private AgentIntegrationTest.Builder passwordBuilder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        manager.before();
        bankIdBuilder =
                new AgentIntegrationTest.Builder("fi", "nordnet-bankid")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);

        passwordBuilder =
                new AgentIntegrationTest.Builder("fi", "nordnet-password")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                        .addCredentialField(Field.Key.PASSWORD, manager.get(Arg.PASSWORD))
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testRefreshBankId() throws Exception {
        bankIdBuilder.build().testRefresh();
    }

    @Test
    public void testRefreshPassword() throws Exception {
        passwordBuilder.build().testRefresh();
    }
}
