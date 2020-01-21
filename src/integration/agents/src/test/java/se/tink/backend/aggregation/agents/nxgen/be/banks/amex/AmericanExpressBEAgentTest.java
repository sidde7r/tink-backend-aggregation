package se.tink.backend.aggregation.agents.nxgen.be.banks.amex;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.be.creditcards.amex.AmericanExpressBEConstants;
import se.tink.libraries.credentials.service.RefreshableItem;

@Ignore
public class AmericanExpressBEAgentTest {

    private ArgumentManager<UsernamePasswordArgumentEnum> manager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

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
                        AmericanExpressBEConstants.MARKET, AmericanExpressBEConstants.PROVIDER_NAME)
                .addCredentialField(
                        Field.Key.USERNAME, manager.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD, manager.get(UsernamePasswordArgumentEnum.PASSWORD))
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
