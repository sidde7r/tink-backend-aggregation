package se.tink.backend.aggregation.agents.nxgen.it.creditcards.amex;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.credentials.service.RefreshableItem;

@Ignore
public class AmericanExpressITAgentTest {

    private enum Arg {
        USERNAME,
        PASSWORD
    }

    private ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    private AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder(
                        AmericanExpressITConstants.MARKET, AmericanExpressITConstants.PROVIDER_NAME)
                .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                .addCredentialField(Field.Key.PASSWORD, manager.get(Arg.PASSWORD))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA);
    }

    @Test
    public void testRefresh() throws Exception {
        builder().build().testRefresh();
    }
}
