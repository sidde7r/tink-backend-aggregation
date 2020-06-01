package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class CreditAgricoleAgentTest {

    private final String USER_ACCOUNT_NUMBER = "123456678901"; // 11 digits
    private final String USER_ACCOUNT_CODE = "123456"; // 6 digits
    private final String APP_CODE = "1234"; // 4 digits

    @Test
    public void testLoginRefresh() throws Exception {
        new AgentIntegrationTest.Builder("fr", Providers.IDF)
                .addCredentialField(Key.USERNAME, USER_ACCOUNT_NUMBER)
                .addCredentialField(Key.PASSWORD, USER_ACCOUNT_CODE)
                .addCredentialField(Key.ACCESS_PIN, APP_CODE)
                .addCredentialField(Key.EMAIL, "test@test.com")
                .addCredentialField(Key.OTP_INPUT, "A1234A")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }

    private static class Providers {
        private static final String IDF = "fr-creditagricolesavoie-password";
    }
}
