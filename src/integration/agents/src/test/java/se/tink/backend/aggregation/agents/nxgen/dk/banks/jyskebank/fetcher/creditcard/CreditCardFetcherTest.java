package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeBankApiClientMockWrapper;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeBankTestData.CreditCardTestData;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CreditCardFetcherTest {
    private JyskeBankApiClientMockWrapper apiClientMockWrapper;
    private JyskeBankCreditCardFetcher fetcher;

    @Before
    public void before() {
        JyskeBankApiClient apiClient = mock(JyskeBankApiClient.class);
        apiClientMockWrapper = new JyskeBankApiClientMockWrapper(apiClient);
        fetcher = new JyskeBankCreditCardFetcher(apiClient);
    }

    @Test
    public void shouldFetchCreditCards() {
        // given
        CreditCardAccount creditCardAccount = getSampleCreditCardAccount();
        apiClientMockWrapper.mockFetchAccountsUsingFile(
                CreditCardTestData.CREDIT_CARD_ACCOUNTS_FILE);

        apiClientMockWrapper.mockFetchTransactionsPageUsingFile(
                creditCardAccount.getFromTemporaryStorage("publicId"),
                CreditCardTestData.CREDIT_CARD_TRANSACTIONS_PAGE_1_FILE,
                1);
        apiClientMockWrapper.mockFetchTransactionsPageUsingFile(
                creditCardAccount.getFromTemporaryStorage("publicId"),
                CreditCardTestData.CREDIT_CARD_TRANSACTIONS_PAGE_2_FILE,
                2);

        // when
        Collection<CreditCardAccount> creditCards = fetcher.fetchAccounts();
        List<? extends Transaction> transactionsPage1 =
                new ArrayList<>(
                        fetcher.getTransactionsFor(creditCardAccount, 1).getTinkTransactions());
        List<? extends Transaction> transactionsPage2 =
                new ArrayList<>(
                        fetcher.getTransactionsFor(creditCardAccount, 2).getTinkTransactions());

        // then
        validateFirstCreditCard(creditCards);
        validateCreditCardTransactions(transactionsPage1, transactionsPage2);
    }

    private void validateCreditCardTransactions(
            List<? extends Transaction> transactionsPage1,
            List<? extends Transaction> transactionsPage2) {
        assertThat(transactionsPage1).hasSize(1);
        assertThat(transactionsPage2).hasSize(1);
        assertThat(transactionsPage1.get(0))
                .isEqualToComparingFieldByFieldRecursively(
                        Transaction.builder()
                                .setAmount(ExactCurrencyAmount.of(41.95, "DKK"))
                                .setPending(false)
                                .setDescription("TEXT")
                                .setDate(LocalDate.parse("2021-04-06"))
                                .build());
        assertThat(transactionsPage2.get(0))
                .isEqualToComparingFieldByFieldRecursively(
                        Transaction.builder()
                                .setAmount(ExactCurrencyAmount.of(-41.95, "DKK"))
                                .setPending(false)
                                .setDescription("TEXT")
                                .setDate(LocalDate.parse("2021-03-01"))
                                .build());
    }

    private void validateFirstCreditCard(Collection<CreditCardAccount> creditCards) {
        Optional<CreditCardAccount> creditCard1 =
                creditCards.stream()
                        .filter(
                                i ->
                                        CreditCardTestData.CREDIT_CARD_1_ID.equals(
                                                i.getIdModule().getUniqueId()))
                        .findAny();
        assertThat(creditCard1.isPresent()).isTrue();
        assertThat(creditCard1.get().getIdModule().getAccountNumber()).isEqualTo("1234567");
        assertThat(creditCard1.get().getName()).isEqualTo("Visa Credit Konto");
    }

    private CreditCardAccount getSampleCreditCardAccount() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber("1234567")
                                .withBalance(ExactCurrencyAmount.inDKK(0))
                                .withAvailableCredit(ExactCurrencyAmount.inDKK(50000))
                                .withCardAlias("Visa Credit Konto")
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier("1234:1234567")
                                .withAccountNumber("1234567")
                                .withAccountName("FIRSTNAME MIDDLENAME LASTNAME")
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.DK,
                                                "1234567",
                                                "Visa Credit Konto"))
                                .build())
                .putInTemporaryStorage(
                        "publicId",
                        "gTLV9Zx4GmM9IeztozuggdhVuZFaNycEDPIz7QjaVCVG9BpFXqCrsTqWjtFQjimUpKuTp6QmY22OfQqeGROUoBXXO6ePBfoufyL22eERG7Y=")
                .build();
    }
}
