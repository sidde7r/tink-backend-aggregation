package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaTestData.ACCOUNT_1_API_ID;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.NordeaAccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class AccountFetcherTest {

    private NordeaAccountFetcher fetcher;

    @Before
    public void before() {
        fetcher = new NordeaAccountFetcher(NordeaDkTestUtils.mockApiClient());
    }

    @Test
    public void shouldFetchAccountsAndMapCorrectly() {
        // when
        Collection<TransactionalAccount> fetchedAccounts = fetcher.fetchAccounts();
        // then
        assertThat(fetchedAccounts).hasSize(3);
        // and
        Optional<TransactionalAccount> account1 =
                fetchedAccounts.stream()
                        .filter(account -> ACCOUNT_1_API_ID.equals(account.getApiIdentifier()))
                        .findAny();
        assertThat(account1.isPresent()).isTrue();
        assertThat(account1.get().getExactBalance().getCurrencyCode()).isEqualTo("DKK");
        assertThat(account1.get().getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("91844.66"));
        assertThat(account1.get().getAccountNumber()).isEqualTo("DK4520007418529630");
        assertThat(account1.get().getIdModule().getUniqueId().equals("7418529630"));
    }

    @Test
    public void shouldFetchTransactionsForAccountAndMapCorrectly() {
        // given
        Collection<TransactionalAccount> fetchedAccounts = fetcher.fetchAccounts();
        TransactionalAccount account1 =
                fetchedAccounts.stream()
                        .filter(account -> ACCOUNT_1_API_ID.equals(account.getApiIdentifier()))
                        .findAny()
                        .orElseThrow(() -> new IllegalStateException("account not found"));
        // when
        List<AggregationTransaction> transactions = fetcher.fetchTransactionsFor(account1);
        // then
        assertThat(transactions).hasSize(4);
        // and
        Optional<AggregationTransaction> transaction1 =
                transactions.stream()
                        .filter(
                                transaction ->
                                        "Bgs From Nordea Pay".equals(transaction.getDescription()))
                        .findAny();
        assertThat(transaction1.isPresent()).isTrue();
        assertThat(transaction1.get().getExactAmount().getCurrencyCode()).isEqualTo("DKK");
        assertThat(transaction1.get().getExactAmount().getExactValue())
                .isEqualByComparingTo(new BigDecimal("6100.00"));
        assertThat(transaction1.get().getDate()).isEqualToIgnoringHours("2020-03-05");
    }
}
