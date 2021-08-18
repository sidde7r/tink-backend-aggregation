package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.TestDataUtils.verifyIdentifiers;
import static se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role.HOLDER;
import static se.tink.libraries.account.enums.AccountIdentifierType.BBAN;
import static se.tink.libraries.account.enums.AccountIdentifierType.IBAN;
import static se.tink.libraries.account.enums.AccountIdentifierType.MASKED_PAN;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.TestDataUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BankdataCreditCardFetcherTest {

    private BankdataApiClient bankdataApiClient;
    private BankdataCreditCardAccountFetcher creditCardAccountFetcher;

    @Before
    public void setUp() {
        bankdataApiClient = mock(BankdataApiClient.class);
        creditCardAccountFetcher = new BankdataCreditCardAccountFetcher(bankdataApiClient);
    }

    @Test
    public void fetchAccountsDataAndCheckIfExactOneIsALoan() {
        // given
        GetAccountsResponse accountsResponse =
                TestDataUtils.readDataFromFile(
                        TestDataUtils.ACCOUNTS_RESP, GetAccountsResponse.class);
        when(bankdataApiClient.getAccounts()).thenReturn(accountsResponse);

        // when
        Collection<CreditCardAccount> cardAccounts = creditCardAccountFetcher.fetchAccounts();

        // then
        assertThat(cardAccounts.size()).isEqualTo(2);
        verifyFirstCard(getCreditCardByAccountNumber(cardAccounts, "526333XXXXXX1234"));
        verifySecondCard(getCreditCardByAccountNumber(cardAccounts, "526333XXXXXX2345"));
    }

    private CreditCardAccount getCreditCardByAccountNumber(
            Collection<CreditCardAccount> accounts, String accountNumber) {
        return accounts.stream()
                .filter(account -> account.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "No credit card with number: " + accountNumber));
    }

    private void verifyFirstCard(CreditCardAccount account) {
        assertThat(account.getType()).isEqualTo(AccountTypes.CREDIT_CARD);
        assertThat(account.getCardModule().getCardNumber()).isEqualTo("526333XXXXXX1234");
        assertThat(account.getCardModule().getBalance())
                .isEqualTo(ExactCurrencyAmount.inDKK(-2072.06));
        assertThat(account.getCardModule().getAvailableCredit())
                .isEqualTo(ExactCurrencyAmount.inDKK(47927.94));
        assertThat(account.getCardModule().getCardAlias()).isEqualTo("Mastercard Gold 1");

        assertThat(account.isUniqueIdentifierEqual("50514969527783")).isTrue();
        assertThat(account.getAccountNumber()).isEqualTo("526333XXXXXX1234");
        assertThat(account.getName()).isEqualTo("Mastercard Gold 1");

        assertThat(account.getParties()).containsExactly(new Party("Account Owner 4", HOLDER));
        verifyIdentifiers(
                account,
                ImmutableMap.of(
                        IBAN, "RINGDK44/DK6850514969527783",
                        BBAN, "50514969527783",
                        MASKED_PAN, "526333XXXXXX1234"));
    }

    private void verifySecondCard(CreditCardAccount account) {
        assertThat(account.getType()).isEqualTo(AccountTypes.CREDIT_CARD);
        assertThat(account.getCardModule().getCardNumber()).isEqualTo("526333XXXXXX2345");
        assertThat(account.getCardModule().getBalance())
                .isEqualTo(ExactCurrencyAmount.inDKK(-2000));
        assertThat(account.getCardModule().getAvailableCredit())
                .isEqualTo(ExactCurrencyAmount.inDKK(58000));
        assertThat(account.getCardModule().getCardAlias()).isEqualTo("Mastercard Gold 2");

        assertThat(account.isUniqueIdentifierEqual("50517786776862")).isTrue();
        assertThat(account.getAccountNumber()).isEqualTo("526333XXXXXX2345");
        assertThat(account.getName()).isEqualTo("Mastercard Gold 2");

        assertThat(account.getParties()).containsExactly(new Party("Account Owner 5", HOLDER));
        verifyIdentifiers(
                account,
                ImmutableMap.of(
                        IBAN, "RINGDK55/DK3650517786776862",
                        BBAN, "50517786776862",
                        MASKED_PAN, "526333XXXXXX2345"));
    }
}
