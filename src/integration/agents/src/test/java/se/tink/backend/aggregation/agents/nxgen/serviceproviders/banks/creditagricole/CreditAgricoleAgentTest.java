package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.payment.rpc.Beneficiary;
import se.tink.libraries.payment.rpc.CreateBeneficiary;

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

    @Test
    public void testCreateBeneficiary() throws Exception {
        final String USER_ACCOUNT = "";
        final String BENEFICIARY_ACCOUNT = "FR7630056005020502000363678"; // Fondation de France

        new AgentIntegrationTest.Builder("fr", Providers.IDF)
                .expectLoggedIn(false)
                .addCredentialField(Key.USERNAME, USER_ACCOUNT_NUMBER)
                .addCredentialField(Key.PASSWORD, USER_ACCOUNT_CODE)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .build()
                .testCreateBeneficiary(
                        CreateBeneficiary.builder()
                                .ownerAccountNumber(USER_ACCOUNT)
                                .beneficiary(
                                        Beneficiary.builder()
                                                .accountNumber(BENEFICIARY_ACCOUNT)
                                                .accountNumberType(AccountIdentifierType.IBAN)
                                                .name("Tink Tester")
                                                .build())
                                .build());
    }

    private static class Providers {
        private static final String IDF = "fr-creditagricolesavoie-password";
    }
}
