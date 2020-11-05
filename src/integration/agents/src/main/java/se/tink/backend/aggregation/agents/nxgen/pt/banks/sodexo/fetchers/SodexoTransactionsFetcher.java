package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.fetchers;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.SodexoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.SodexoConstants;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SodexoTransactionsFetcher implements TransactionFetcher<TransactionalAccount> {

    private final SodexoApiClient sodexoApiClient;

    public SodexoTransactionsFetcher(SodexoApiClient sodexoApiClient) {
        this.sodexoApiClient = sodexoApiClient;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {

        TransactionResponse transactionResponse = sodexoApiClient.getTransactions();

        return transactionResponse.getTransactions().stream()
                .map(this::mapTransaction)
                .collect(Collectors.toList());
    }

    private AggregationTransaction mapTransaction(TransactionEntity transactionEntity) {

        ExactCurrencyAmount exactCurrencyAmount =
                ExactCurrencyAmount.of(transactionEntity.getAmount(), SodexoConstants.CURRENCY);

        return Transaction.builder()
                .setDescription(transactionEntity.getDescription())
                .setDate(transactionEntity.getDateIso())
                .setAmount(exactCurrencyAmount)
                .build();
    }
}
