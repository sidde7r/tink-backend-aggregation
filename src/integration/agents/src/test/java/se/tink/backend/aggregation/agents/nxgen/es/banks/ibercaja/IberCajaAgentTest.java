package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.credentials.service.RefreshableItem;

@Ignore
public class IberCajaAgentTest {

    private enum Arg {
        USERNAME,
        PASSWORD
    }

    private final ArgumentManager<IberCajaAgentTest.Arg> helper =
            new ArgumentManager<>(IberCajaAgentTest.Arg.values());

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("es", "es-ibercaja-password")
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(false);

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, helper.get(Arg.USERNAME))
                .addCredentialField(Field.Key.PASSWORD, helper.get(Arg.PASSWORD))
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }
}
