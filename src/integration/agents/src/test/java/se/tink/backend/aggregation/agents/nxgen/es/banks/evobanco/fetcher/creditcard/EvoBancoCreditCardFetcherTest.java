package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EvoBancoCreditCardFetcherTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/evobanco/resources";

    private static GlobalPositionResponse accountsCorrectResponse;
    private EvoBancoApiClient evoBancoApiClient;
    private EvoBancoCreditCardFetcher accountFetcher;

    @BeforeClass
    public static void setUpOnce() {
        accountsCorrectResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "accounts_correct_response.json").toFile(),
                        GlobalPositionResponse.class);
    }

    @Before
    public void setup() {
        evoBancoApiClient = mock(EvoBancoApiClient.class);
        accountFetcher = new EvoBancoCreditCardFetcher(evoBancoApiClient);

        given(evoBancoApiClient.globalPosition()).willReturn(accountsCorrectResponse);
    }

    @Test
    public void shouldFetchAndMapAccounts() {
        // when
        Collection<CreditCardAccount> accounts = accountFetcher.fetchAccounts();
        accounts.iterator();

        // then
        Iterator<CreditCardAccount> iterator = accounts.iterator();
        assertCreditCardAccount1(iterator.next());
        assertCreditCardAccount2(iterator.next());
    }

    @Test
    public void shouldReturnOnlyAccountsWithKnownAccountType() {
        // when
        Collection<CreditCardAccount> accounts = accountFetcher.fetchAccounts();

        // then
        assertThat(accounts).hasSize(2);
    }

    @Test
    public void shouldReturnFalseAndTransactionsWhenIsTheLastTransactionPage() {
        // given
        Collection<CreditCardAccount> accounts = accountFetcher.fetchAccounts();
        CreditCardAccount card = accounts.iterator().next();

        CardTransactionsResponse cardTrxResp =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "last_credit_card_transactions_page.json")
                                .toFile(),
                        CardTransactionsResponse.class);
        EvoBancoCreditCardFetcher fetcher = new EvoBancoCreditCardFetcher(evoBancoApiClient);
        given(evoBancoApiClient.getEEHeaders()).willReturn(new HashMap<>());
        given(evoBancoApiClient.fetchCardTransactions(card.getApiIdentifier(), 0))
                .willReturn(cardTrxResp);

        // when
        PaginatorResponse response = fetcher.getTransactionsFor(card, 0);

        // then
        assertThat(response.getTinkTransactions()).hasSize(27);
        assertThat(response.canFetchMore().get()).isFalse();
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenTheResponseThrowsNoMoreTransactionsError() {
        // given
        Collection<CreditCardAccount> accounts = accountFetcher.fetchAccounts();
        CreditCardAccount card = accounts.iterator().next();

        CardTransactionsResponse cardTrxResp =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "no_transactions_response.json").toFile(),
                        CardTransactionsResponse.class);

        EvoBancoCreditCardFetcher fetcher = new EvoBancoCreditCardFetcher(evoBancoApiClient);
        given(evoBancoApiClient.getEEHeaders()).willReturn(new HashMap<>());
        given(evoBancoApiClient.fetchCardTransactions(card.getApiIdentifier(), 0))
                .willReturn(cardTrxResp);

        // when
        ThrowingCallable callable = () -> fetcher.getTransactionsFor(card, 0);

        // then
        Assertions.assertThatThrownBy(callable).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void shouldThrowBankSideExceptionWhenTheResponseStatusIs500() {
        // given
        Collection<CreditCardAccount> accounts = accountFetcher.fetchAccounts();
        CreditCardAccount card = accounts.iterator().next();

        HttpResponseException exception = mock(HttpResponseException.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        given(evoBancoApiClient.getEEHeaders()).willReturn(new HashMap<>());
        given(evoBancoApiClient.fetchCardTransactions(card.getApiIdentifier(), 0))
                .willThrow(exception);
        given(exception.getResponse()).willReturn(httpResponse);
        given(httpResponse.getStatus()).willReturn(500);

        EvoBancoCreditCardFetcher fetcher = new EvoBancoCreditCardFetcher(evoBancoApiClient);

        // when
        ThrowingCallable callable = () -> fetcher.getTransactionsFor(card, 0);

        // then
        Assertions.assertThatThrownBy(callable).isInstanceOf(BankServiceException.class);
    }

    private void assertCreditCardAccount1(CreditCardAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("58.05"));
        assertThat(account.getExactAvailableCredit().getExactValue())
                .isEqualByComparingTo(new BigDecimal("56.79"));
        assertThat(account.getParties().get(0).getName()).isEqualToIgnoringCase("SZYMON MYSIAK");
        assertThat(account.getFromTemporaryStorage(EvoBancoConstants.Storage.CARD_STATE))
                .isEqualTo("PODER CLI.");
        assertThat(account.getAccountNumber()).isEqualTo("9999 **** **** 6999");
        assertThat(account.getName()).isEqualTo("TARJETA DE DÉBITO *6999");
    }

    private void assertCreditCardAccount2(CreditCardAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("546.56"));
        assertThat(account.getExactAvailableCredit().getExactValue())
                .isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(account.getParties().get(0).getName()).isEqualToIgnoringCase("SZYMON MYSIAK");
        assertThat(account.getFromTemporaryStorage(EvoBancoConstants.Storage.CARD_STATE))
                .isEqualTo("E.EST.RENO");
        assertThat(account.getAccountNumber()).isEqualTo("6367 **** **** 4455");
        assertThat(account.getName()).isEqualTo("TARJETA MIXTA *4455");
    }
}
