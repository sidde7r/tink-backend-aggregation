package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class CreditAgricoleAgentTest {

    private final String USER_ACCOUNT_NUMBER = "";  // 11 digits
    private final String USER_ACCOUNT_CODE = "";    // 6 digits
    private final String APP_CODE = "";             // 4 digits



    @Test
    public void testLoginRefresh() throws Exception {
        new AgentIntegrationTest.Builder("fr", Providers.IDF)
                .addCredentialField(CreditAgricoleConstants.StorageKey.USER_ACCOUNT_NUMBER, USER_ACCOUNT_NUMBER)
                .addCredentialField(CreditAgricoleConstants.StorageKey.USER_ACCOUNT_CODE, USER_ACCOUNT_CODE)
                .addCredentialField(CreditAgricoleConstants.StorageKey.APP_CODE, APP_CODE)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }

    private static class Providers {
        private static final String IDF = "fr-creditagricoleiledefrance-password";
    }
}
