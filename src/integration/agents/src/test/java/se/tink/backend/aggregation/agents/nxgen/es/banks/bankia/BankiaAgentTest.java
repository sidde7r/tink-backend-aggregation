package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.credentials.service.RefreshableItem;

@Ignore
public class BankiaAgentTest {

    private AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder(
                        BankiaConstants.MARKET, BankiaConstants.PROVIDER_NAME)
                .addCredentialField(Field.Key.USERNAME, "")
                .addCredentialField(Field.Key.PASSWORD, "")
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
