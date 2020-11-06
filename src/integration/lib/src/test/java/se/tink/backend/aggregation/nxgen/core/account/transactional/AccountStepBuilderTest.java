package se.tink.backend.aggregation.nxgen.core.account.transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.CheckingBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.SavingsBuildStep;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

public final class AccountStepBuilderTest {

    private static final String VALID_UNIQUE_ID = "MyUniqueId";
    private static final String VALID_ACCOUNT_NUMBER = "MyAccountNumber";
    private final Amount VALID_AMOUNT = Amount.inSEK(10);
    private final ExactCurrencyAmount VALID_EXACT_AMOUNT = ExactCurrencyAmount.of(10, "SEK");
    private final AccountIdentifier VALID_IBAN_IDENTIFIER =
            AccountIdentifier.create(AccountIdentifier.Type.IBAN, "SE1004976016582303953969");
    private final AccountIdentifier VALID_TINK_IDENTIFIER =
            AccountIdentifier.create(
                    AccountIdentifier.Type.TINK, "2ba008c2-05a6-4cec-8792-4e8528ac0699");

    private static final String VALID_ALIAS = "MyAlias";
    private static final String VALID_API_IDENTIFIER = "MyApiIdentifier";
    private static final String VALID_PRODUCT_NAME = "MyProductName";
    private static final String VALID_HOLDER_NAME = "MyHolderName";
    private static final Double VALID_INTEREST_RATE = 1.5;

    /** @return Minimum viable builder for checking account. */
    private CheckingBuildStep getRequiredCheckingBuilder() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(VALID_UNIQUE_ID)
                .setAccountNumber(VALID_ACCOUNT_NUMBER)
                .setBalance(VALID_AMOUNT)
                .setAlias(VALID_ALIAS)
                .addAccountIdentifier(VALID_IBAN_IDENTIFIER);
    }

    /** @return Minimum viable builder for savings account. */
    private SavingsBuildStep getRequiredSavingsBuilder() {
        return SavingsAccount.builder()
                .setUniqueIdentifier(VALID_UNIQUE_ID)
                .setAccountNumber(VALID_ACCOUNT_NUMBER)
                .setBalance(VALID_AMOUNT)
                .setAlias(VALID_ALIAS)
                .addAccountIdentifier(VALID_IBAN_IDENTIFIER);
    }

    @Test
    public void ensureSet_mandatoryFields_CheckingAccount() {

        CheckingAccount account = getRequiredCheckingBuilder().build();

        assertEquals(account.hashCode(), VALID_UNIQUE_ID.hashCode());
        assertEquals(account.getAccountNumber(), VALID_ACCOUNT_NUMBER);
        assertEquals(account.getExactBalance(), VALID_EXACT_AMOUNT);
        assertEquals(account.getIdentifiers(), Collections.singletonList(VALID_IBAN_IDENTIFIER));
    }

    @Test
    public void ensureSet_mandatoryFields_SavingsAccount() {

        SavingsAccount account = getRequiredSavingsBuilder().build();

        assertEquals(account.hashCode(), VALID_UNIQUE_ID.hashCode());
        assertEquals(account.getAccountNumber(), VALID_ACCOUNT_NUMBER);
        assertEquals(account.getExactBalance(), VALID_EXACT_AMOUNT);
        assertEquals(account.getIdentifiers(), Collections.singletonList(VALID_IBAN_IDENTIFIER));
    }

    @Test
    public void ensureSet_optionalFields_CheckingAccount() {
        CheckingAccount account =
                getRequiredCheckingBuilder()
                        .setApiIdentifier(VALID_API_IDENTIFIER)
                        .setProductName(VALID_PRODUCT_NAME)
                        .addHolderName(VALID_HOLDER_NAME)
                        .build();

        assertEquals(account.getName(), VALID_ALIAS);
        assertEquals(account.getApiIdentifier(), VALID_API_IDENTIFIER);
        assertEquals(account.getProductName(), VALID_PRODUCT_NAME);
        assertEquals(account.getHolderName().toString(), VALID_HOLDER_NAME);
    }

    @Test
    public void ensureSet_optionalFields_SavingsAccount() {
        SavingsAccount account =
                getRequiredSavingsBuilder()
                        .setApiIdentifier(VALID_API_IDENTIFIER)
                        .setProductName(VALID_PRODUCT_NAME)
                        .addHolderName(VALID_HOLDER_NAME)
                        .setInterestRate(VALID_INTEREST_RATE)
                        .build();

        assertEquals(account.getName(), VALID_ALIAS);
        assertEquals(account.getApiIdentifier(), VALID_API_IDENTIFIER);
        assertEquals(account.getProductName(), VALID_PRODUCT_NAME);
        assertEquals(account.getHolderName().toString(), VALID_HOLDER_NAME);
        assertEquals(account.getInterestRate(), VALID_INTEREST_RATE, 0.0);
    }

    @Test
    public void ensureSet_tempStorage() {

        final String KEY_1 = "key_1";
        final String KEY_2 = "key_2";
        final String VALUE_1 = "value_1";
        final String VALUE_2 = "value_2";

        Account account =
                getRequiredCheckingBuilder()
                        .putInTemporaryStorage(KEY_1, VALUE_1)
                        .putInTemporaryStorage(KEY_2, VALUE_2)
                        .build();

        assertEquals(account.getFromTemporaryStorage(KEY_1), VALUE_1);
        assertEquals(account.getFromTemporaryStorage(KEY_2), VALUE_2);
    }

    @Test
    public void ensureSet_accountFlags() {

        Account account =
                getRequiredCheckingBuilder()
                        .addAccountFlags(AccountFlag.BUSINESS, AccountFlag.MANDATE)
                        .build();

        assertTrue(
                account.getAccountFlags()
                        .containsAll(Arrays.asList(AccountFlag.BUSINESS, AccountFlag.MANDATE)));
    }

    @Test
    public void ensureUniqueId_IsSanitized() {
        final String formattedId = "abc-123_DEF T";

        SavingsAccount account =
                SavingsAccount.builder()
                        .setUniqueIdentifier(formattedId)
                        .setAccountNumber(VALID_ACCOUNT_NUMBER)
                        .setBalance(VALID_AMOUNT)
                        .setAlias(VALID_ALIAS)
                        .addAccountIdentifier(VALID_IBAN_IDENTIFIER)
                        .build();

        assertEquals(account.hashCode(), StringUtils.removeNonAlphaNumeric(formattedId).hashCode());
    }

    @Test
    public void ensureAlias_null_FallsBackToAccountNumber() {

        SavingsAccount account =
                SavingsAccount.builder()
                        .setUniqueIdentifier(VALID_UNIQUE_ID)
                        .setAccountNumber(VALID_ACCOUNT_NUMBER)
                        .setBalance(VALID_AMOUNT)
                        .setAlias(null)
                        .addAccountIdentifier(VALID_TINK_IDENTIFIER)
                        .build();
        assertEquals(VALID_ACCOUNT_NUMBER, account.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureUniqueId_null_ThrowsException() {

        SavingsAccount.builder().setUniqueIdentifier(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureUniqueId_empty_ThrowsException() {

        SavingsAccount.builder().setUniqueIdentifier("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureUniqueId_onlyFormattingChars_ThrowsException() {

        SavingsAccount.builder().setUniqueIdentifier("- _");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAccountNumber_null_ThrowsException() {

        SavingsAccount.builder().setUniqueIdentifier(VALID_UNIQUE_ID).setAccountNumber(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAccountNumber_empty_ThrowsException() {

        SavingsAccount.builder().setUniqueIdentifier(VALID_UNIQUE_ID).setAccountNumber("");
    }

    @Test(expected = NullPointerException.class)
    public void ensureBalance_null_ThrowsException() {

        SavingsAccount.builder()
                .setUniqueIdentifier(VALID_UNIQUE_ID)
                .setAccountNumber(VALID_ACCOUNT_NUMBER)
                .setBalance(null);
    }

    @Test
    public void ensureIdentifiers_two_isOk() {

        Account account =
                SavingsAccount.builder()
                        .setUniqueIdentifier(VALID_UNIQUE_ID)
                        .setAccountNumber(VALID_ACCOUNT_NUMBER)
                        .setBalance(VALID_AMOUNT)
                        .setAlias(VALID_ALIAS)
                        .addAccountIdentifier(VALID_IBAN_IDENTIFIER)
                        .addAccountIdentifier(VALID_TINK_IDENTIFIER)
                        .build();

        assertTrue(
                account.getIdentifiers()
                        .containsAll(Arrays.asList(VALID_IBAN_IDENTIFIER, VALID_TINK_IDENTIFIER)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIdentifiers_duplicate_throwsException() {

        SavingsAccount.builder()
                .setUniqueIdentifier(VALID_UNIQUE_ID)
                .setAccountNumber(VALID_ACCOUNT_NUMBER)
                .setBalance(VALID_AMOUNT)
                .setAlias(VALID_ALIAS)
                .addAccountIdentifier(VALID_IBAN_IDENTIFIER)
                .addAccountIdentifier(VALID_IBAN_IDENTIFIER)
                .build();
    }
}
