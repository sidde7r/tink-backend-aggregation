package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.credentials.service.RefreshableItem;

@Ignore
public class CreditAgricoleAgentTest {

    private final String USER_ACCOUNT_NUMBER = ""; // 11 digits
    private final String USER_ACCOUNT_CODE = ""; // 6 digits

    @Test
    public void testLoginRefresh() throws Exception {
        new AgentIntegrationTest.Builder("fr", Providers.IDF)
                .addCredentialField(Key.USERNAME, USER_ACCOUNT_NUMBER)
                .addCredentialField(Key.PASSWORD, USER_ACCOUNT_CODE)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }

    private static class Providers {
        private static final String IDF = "fr-creditagricolecentreest-password";
    }
}
