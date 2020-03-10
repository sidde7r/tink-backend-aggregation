package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.NordeaCreditCardFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class CreditCardFetcherTest {

    private NordeaCreditCardFetcher fetcher;

    @Before
    public void before() {
        fetcher = new NordeaCreditCardFetcher(NordeaDkTestUtils.mockApiClient());
    }

    @Test
    public void shouldFetchCreditCards() {
        // when
        Collection<CreditCardAccount> creditCards = fetcher.fetchAccounts();
        // then
        assertThat(creditCards).hasSize(1);
        // and
        Optional<CreditCardAccount> creditCard =
                creditCards.stream()
                        .filter(a -> "5678".equals(a.getIdModule().getUniqueId()))
                        .findAny();
        assertThat(creditCard.isPresent()).isTrue();
        assertThat(creditCard.get().getIdModule().getUniqueId()).isEqualTo("5678");
        assertThat(creditCard.get().getIdModule().getAccountNumber())
                .isEqualTo("1234 **** **** 5678");
        assertThat(creditCard.get().getCardModule().getCardNumber())
                .isEqualTo("1234 **** **** 5678");
        assertThat(creditCard.get().getCardModule().getBalance().getCurrencyCode())
                .isEqualTo("DKK");
        assertThat(creditCard.get().getCardModule().getBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(creditCard.get().getCardModule().getAvailableCredit().getCurrencyCode())
                .isEqualTo("DKK");
        assertThat(creditCard.get().getCardModule().getAvailableCredit().getExactValue())
                .isEqualByComparingTo(new BigDecimal("40000"));
    }

    @Test
    public void shouldFetchCreditCardTransactions() {
        // given
        Collection<CreditCardAccount> creditCards = fetcher.fetchAccounts();
        CreditCardAccount creditCard =
                creditCards.stream()
                        .filter(a -> "5678".equals(a.getIdModule().getUniqueId()))
                        .findAny()
                        .orElseThrow(() -> new IllegalStateException("credit card not found"));
        // when
        List<AggregationTransaction> transactions = fetcher.fetchTransactionsFor(creditCard);
        assertThat(transactions).hasSize(3);
        // and
        Optional<AggregationTransaction> transaction =
                transactions.stream()
                        .filter(t -> "Very good pizza".equals(t.getDescription()))
                        .findAny();
        assertThat(transaction.isPresent()).isTrue();
        assertThat(transaction.get().getDate()).isEqualToIgnoringHours("2020-02-17");
        assertThat(transaction.get().getExactAmount().getCurrencyCode()).isEqualTo("DKK");
        assertThat(transaction.get().getExactAmount().getExactValue())
                .isEqualByComparingTo(new BigDecimal("-410.00"));
    }
}
