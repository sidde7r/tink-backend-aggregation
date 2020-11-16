package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.SodexoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.SodexoStorage;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.fetchers.SodexoTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RunWith(MockitoJUnitRunner.class)
public class SodexoTransactionsFetcherTest {

    @Mock private SodexoApiClient sodexoApiClient;

    @Mock private SodexoStorage sodexoStorage;

    @InjectMocks private SodexoTransactionsFetcher sodexoTransactionsFetcher;

    @Test
    public void shouldFetchTransaction() {
        // given
        when(sodexoApiClient.getTransactions()).thenReturn(mockTransactionResponse());

        // when
        List<AggregationTransaction> aggregationTransactions =
                sodexoTransactionsFetcher.fetchTransactionsFor(buildAccount());

        // then
        assertThat(aggregationTransactions).isNotNull().hasSize(1);

        assertThat(aggregationTransactions.get(0).getDate().toString())
                .isEqualTo("Sat Jan 01 11:00:00 UTC 2000");
        assertThat(aggregationTransactions.get(0).getDescription()).isEqualTo("description");
        assertThat(aggregationTransactions.get(0).getExactAmount().getDoubleValue()).isEqualTo(420);
    }

    private TransactionResponse mockTransactionResponse() {
        TransactionResponse transactionResponse = new TransactionResponse();
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setAmount(420);
        transactionEntity.setDate("01-01-2000");
        try {
            transactionEntity.setDateIso(
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2000-01-01 00:00:00"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        transactionEntity.setDescription("description");
        transactionEntity.setType(1);
        transactionResponse.setTransactions(Collections.singletonList(transactionEntity));
        return transactionResponse;
    }

    private TransactionalAccount buildAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withoutFlags()
                .withBalance(BalanceModule.of(ExactCurrencyAmount.inEUR(420)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier("123")
                                .withAccountNumber("456")
                                .withAccountName("AccName")
                                .addIdentifier(AccountIdentifier.create(Type.TINK, "420"))
                                .build())
                .setApiIdentifier("111")
                .build()
                .orElse(null);
    }
}
