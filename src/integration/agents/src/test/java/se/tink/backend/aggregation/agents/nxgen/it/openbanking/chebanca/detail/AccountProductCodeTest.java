package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.data.AccountProductCodeTestData;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.AccountEntity;

public class AccountProductCodeTest {

    @Test
    public void shouldRecognizeCheckingAccountAndOtherAccount() {
        for (String productCode : AccountProductCodeTestData.getCheckingCodes()) {
            // given
            AccountEntity accountEntity =
                    AccountProductCodeTestData.getCheckingAccountEntity(productCode);

            // when
            boolean isCheckingAccount = AccountProductCode.isCheckingAccount(accountEntity);
            boolean isSavingsAccount = AccountProductCode.isSavingsAccount(accountEntity);
            boolean isCreditCardAccount = AccountProductCode.isCreditCardAccount(accountEntity);

            // then
            assertTrue(isCheckingAccount);
            assertFalse(isSavingsAccount);
            assertFalse(isCreditCardAccount);
        }
    }

    @Test
    public void shouldRecognizeSavingsAccountAndNotOtherAccount() {
        for (String productCode : AccountProductCodeTestData.getSavingsCodes()) {
            // given
            AccountEntity accountEntity =
                    AccountProductCodeTestData.getSavingsAccountEntity(productCode);

            // when
            boolean isSavingsAccount = AccountProductCode.isSavingsAccount(accountEntity);
            boolean isCheckingAccount = AccountProductCode.isCheckingAccount(accountEntity);
            boolean isCreditCardAccount = AccountProductCode.isCreditCardAccount(accountEntity);

            // then
            assertTrue(isSavingsAccount);
            assertFalse(isCheckingAccount);
            assertFalse(isCreditCardAccount);
        }
    }

    @Test
    public void shouldRecognizeCreditCardAccountAndNotOtherAccount() {
        for (String productCode : AccountProductCodeTestData.getCreditCardCodes()) {
            // given
            AccountEntity accountEntity =
                    AccountProductCodeTestData.getCreditCardAccountEntity(productCode);

            // when
            boolean isSavingsAccount = AccountProductCode.isSavingsAccount(accountEntity);
            boolean isCheckingAccount = AccountProductCode.isCheckingAccount(accountEntity);
            boolean isCreditCardAccount = AccountProductCode.isCreditCardAccount(accountEntity);

            // then
            assertTrue(isCreditCardAccount);
            assertFalse(isCheckingAccount);
            assertFalse(isSavingsAccount);
        }
    }

    @Test
    public void shouldNotRecognizeFakeProductCode() {
        // given
        AccountEntity accountEntity =
                AccountProductCodeTestData.getSavingsAccountEntity("none_existing_code");

        // when
        boolean isSavingsAccount = AccountProductCode.isSavingsAccount(accountEntity);
        boolean isCheckingAccount = AccountProductCode.isCheckingAccount(accountEntity);
        boolean isCreditCardAccount = AccountProductCode.isCreditCardAccount(accountEntity);

        // then
        assertFalse(isCreditCardAccount);
        assertFalse(isCheckingAccount);
        assertFalse(isSavingsAccount);
    }
}
