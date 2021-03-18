package se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.EdenredApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.entities.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.storage.EdenredStorage;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RunWith(MockitoJUnitRunner.class)
public class EdenredTransactionsFetcherTest {

    @Mock private EdenredApiClient edenredApiClient;

    @Mock private EdenredStorage edenredStorage;

    @InjectMocks EdenredTransactionsFetcher fetcher;

    @Test
    public void shouldFetchTransactionsFromStorage() {
        when(edenredStorage.getTransactions(eq(123L)))
                .thenReturn(Optional.of(mockTransactionsResponse().getData()));

        List<AggregationTransaction> aggregationTransactions =
                fetcher.fetchTransactionsFor(buildAccount());

        assertThat(aggregationTransactions).isNotNull().hasSize(1);

        assertThat(aggregationTransactions.get(0).getDescription()).isEqualTo("Some name");
        assertThat(aggregationTransactions.get(0).getExactAmount().getDoubleValue())
                .isEqualTo(50.50);

        verify(edenredApiClient, never()).getTransactions(anyLong());
    }

    @Test
    public void shouldFetchTransactionsFromBackend() {
        when(edenredApiClient.getTransactions(eq(123L))).thenReturn(mockTransactionsResponse());

        List<AggregationTransaction> aggregationTransactions =
                fetcher.fetchTransactionsFor(buildAccount());

        assertThat(aggregationTransactions).isNotNull().hasSize(1);

        assertThat(aggregationTransactions.get(0).getDescription()).isEqualTo("Some name");
        assertThat(aggregationTransactions.get(0).getExactAmount().getDoubleValue())
                .isEqualTo(50.50);
    }

    private TransactionalAccount buildAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withoutFlags()
                .withBalance(BalanceModule.of(ExactCurrencyAmount.inEUR(123.45)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier("123")
                                .withAccountNumber("456")
                                .withAccountName("Something")
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifierType.IBAN, "456"))
                                .build())
                .setApiIdentifier("123")
                .build()
                .orElse(null);
    }

    private TransactionsResponse mockTransactionsResponse() {
        TransactionsResponse transactionsResponse = new TransactionsResponse();
        transactionsResponse.setData(
                TransactionsEntity.builder()
                        .account(AccountEntity.builder().availableBalance(132.45).build())
                        .movementList(
                                Collections.singletonList(
                                        TransactionEntity.builder()
                                                .amount(50.50)
                                                .transactionName("Some name")
                                                .build()))
                        .build());
        return transactionsResponse;
    }
}
