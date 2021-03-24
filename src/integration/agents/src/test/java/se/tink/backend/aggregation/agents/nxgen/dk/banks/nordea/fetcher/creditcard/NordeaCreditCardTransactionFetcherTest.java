package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClientMockWrapper;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaTestData.CreditCardTestData;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class NordeaCreditCardTransactionFetcherTest {

    private NordeaDkApiClientMockWrapper apiClientMockWrapper;
    private NordeaCreditCardTransactionFetcher fetcher;

    @Before
    public void before() {
        NordeaDkApiClient apiClientMock = mock(NordeaDkApiClient.class);
        apiClientMockWrapper = new NordeaDkApiClientMockWrapper(apiClientMock);
        fetcher = new NordeaCreditCardTransactionFetcher(apiClientMock);
    }

    @Test
    public void shouldFetchCreditCardTransactionsAndMapToTinkModel() {
        // given
        CreditCardAccount creditCardAccount = getSampleCreditCardAccount();

        apiClientMockWrapper.mockFetchCreditCardTransactionsPageUsingFile(
                creditCardAccount.getApiIdentifier(),
                1,
                CreditCardTestData.CREDIT_CARD_TRANSACTIONS_PAGE_1_FILE);
        apiClientMockWrapper.mockFetchCreditCardTransactionsPageUsingFile(
                creditCardAccount.getApiIdentifier(),
                2,
                CreditCardTestData.CREDIT_CARD_TRANSACTIONS_PAGE_2_FILE);

        // when
        List<? extends Transaction> transactionsPage1 =
                new ArrayList<>(
                        fetcher.getTransactionsFor(creditCardAccount, null).getTinkTransactions());
        List<? extends Transaction> transactionsPage2 =
                new ArrayList<>(
                        fetcher.getTransactionsFor(creditCardAccount, 2).getTinkTransactions());

        // then
        assertThat(transactionsPage1).hasSize(2);
        assertThat(transactionsPage1.get(0))
                .isEqualToComparingFieldByFieldRecursively(
                        Transaction.builder()
                                .setAmount(
                                        ExactCurrencyAmount.of(-410.00, NordeaDkConstants.CURRENCY))
                                .setPending(false)
                                .setDescription("Very good pizza")
                                .setDate(LocalDate.parse("2020-02-17"))
                                .build());
        assertThat(transactionsPage1.get(1))
                .isEqualToComparingFieldByFieldRecursively(
                        Transaction.builder()
                                .setAmount(
                                        ExactCurrencyAmount.of(-578.00, NordeaDkConstants.CURRENCY))
                                .setPending(true)
                                .setDescription("Awesome burger")
                                .setDate(LocalDate.parse("2020-02-14"))
                                .build());
        // and
        assertThat(transactionsPage2).hasSize(1);
        assertThat(transactionsPage2.get(0))
                .isEqualToComparingFieldByFieldRecursively(
                        Transaction.builder()
                                .setAmount(
                                        ExactCurrencyAmount.of(-111.00, NordeaDkConstants.CURRENCY))
                                .setPending(false)
                                .setDescription("Something from page 2")
                                .setDate(LocalDate.parse("2020-02-17"))
                                .build());
    }

    @Test
    public void shouldFetchCreditCardTransactionsWithoutDate() {
        // given
        CreditCardAccount creditCardAccount = getSampleCreditCardAccount();
        apiClientMockWrapper.mockFetchCreditCardTransactionsPageUsingFile(
                creditCardAccount.getApiIdentifier(),
                1,
                CreditCardTestData.CREDIT_CARD_TRANSACTIONS_WITHOUT_DATE_FILE);

        // when
        List<? extends Transaction> transactions =
                new ArrayList<>(
                        fetcher.getTransactionsFor(creditCardAccount, 1).getTinkTransactions());

        // then
        assertThat(transactions).hasSize(2);
    }

    private CreditCardAccount getSampleCreditCardAccount() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber("1234")
                                .withBalance(ExactCurrencyAmount.inDKK(0))
                                .withAvailableCredit(ExactCurrencyAmount.inDKK(0))
                                .withCardAlias("CARD_ALIAS")
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier("UNIQUE_IDENTIFIER")
                                .withAccountNumber("ACCOUNT_NUMBER")
                                .withAccountName("ACCOUNT_NAME")
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.BBAN, "BBAN"))
                                .build())
                .setApiIdentifier("CREDIT_CARD_ID_WITH_TRANSACTIONS_WITHOUT_DATE")
                .build();
    }
}
