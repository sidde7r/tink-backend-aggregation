package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.transactional.detail.AccountsTestData.ACCOUNT_1_CURRENCY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.transactional.detail.AccountsTestData.PAYLOAD_ACCOUNT_ID_1;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.transactional.detail.AccountsTestData;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.detail.TransactionDto;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.NovoBancoTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.MovementsEntity;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.pair.Pair;

public class NovoBancoTransactionFetcherTest {
    @Test
    public void shouldReturnEmptyCollectionIfNoTransactionsAvailable() {
        // given
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        when(apiClient.getTransactions(any()))
                .thenReturn(Pair.of(Collections.emptyList(), ACCOUNT_1_CURRENCY));
        NovoBancoTransactionFetcher fetcher = new NovoBancoTransactionFetcher(apiClient);

        // when
        Collection<? extends Transaction> transactions =
                fetcher.getTransactionsFor(getAccount(PAYLOAD_ACCOUNT_ID_1, ACCOUNT_1_CURRENCY), 1)
                        .getTinkTransactions();

        // then
        assertTrue(transactions.isEmpty());
    }

    @Test
    public void shouldReturnNonEmptyCollectionIfTransactionsAvailable() {
        // given
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        List<MovementsEntity> movements =
                AccountsTestData.getResponse(PAYLOAD_ACCOUNT_ID_1).getBody().getMovements();
        when(apiClient.getTransactions(PAYLOAD_ACCOUNT_ID_1))
                .thenReturn(Pair.of(movements, ACCOUNT_1_CURRENCY));
        NovoBancoTransactionFetcher fetcher = new NovoBancoTransactionFetcher(apiClient);

        // when
        Collection<? extends Transaction> transactions =
                fetcher.getTransactionsFor(getAccount(PAYLOAD_ACCOUNT_ID_1, ACCOUNT_1_CURRENCY), 1)
                        .getTinkTransactions();

        // then
        assertFalse(transactions.isEmpty());
    }

    @Test
    public void shouldReturnCorrectlyMappedTransactions() {
        // given
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        List<MovementsEntity> movements =
                AccountsTestData.getResponse(PAYLOAD_ACCOUNT_ID_1).getBody().getMovements();
        when(apiClient.getTransactions(PAYLOAD_ACCOUNT_ID_1))
                .thenReturn(Pair.of(movements, ACCOUNT_1_CURRENCY));
        NovoBancoTransactionFetcher fetcher = new NovoBancoTransactionFetcher(apiClient);

        // when
        Collection<? extends Transaction> transactions =
                fetcher.getTransactionsFor(getAccount(PAYLOAD_ACCOUNT_ID_1, ACCOUNT_1_CURRENCY), 1)
                        .getTinkTransactions();
        // then
        assertTransactionsEqual(transactions);
    }

    private void assertTransactionsEqual(Collection<? extends Transaction> transactions) {
        Collection<TransactionDto> refTransactions = AccountsTestData.getReferenceTransactionDtos();
        assertEquals(refTransactions.size(), transactions.size());
        refTransactions.forEach(
                refTransaction -> {
                    Transaction transaction = getMatchingTransaction(refTransaction, transactions);
                    assertTransactionEquals(refTransaction, transaction);
                });
    }

    private void assertTransactionEquals(TransactionDto refTransaction, Transaction transaction) {
        assertNotNull("No transaction matching the reference description", transaction);
        assertEquals(refTransaction.getDate(), dateToString(transaction.getDate()));
        assertEquals(refTransaction.getAmount(), transaction.getExactAmount());
    }

    private Transaction getMatchingTransaction(
            TransactionDto refTransaction, Collection<? extends Transaction> transactions) {
        String refDescription = refTransaction.getDescription();
        return transactions.stream()
                .filter(t -> refDescription.equals(((Transaction) t).getDescription()))
                .findFirst()
                .orElse(null);
    }

    private String dateToString(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private TransactionalAccount getAccount(String accountId, String currency) {
        IdModule id =
                IdModule.builder()
                        .withUniqueIdentifier(accountId)
                        .withAccountNumber(accountId)
                        .withAccountName(accountId)
                        .addIdentifier(
                                AccountIdentifier.create(
                                        AccountIdentifier.Type.COUNTRY_SPECIFIC, accountId))
                        .build();
        BalanceModule balance =
                BalanceModule.builder().withBalance(ExactCurrencyAmount.zero(currency)).build();
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withoutFlags()
                .withBalance(balance)
                .withId(id)
                .build()
                .get();
    }
}
