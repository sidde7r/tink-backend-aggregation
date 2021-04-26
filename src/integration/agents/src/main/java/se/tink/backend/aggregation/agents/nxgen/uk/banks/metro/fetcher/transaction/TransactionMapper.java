package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction;

import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.model.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionMapper {
    private static final String PROVIDER_MARKET = "UK";

    AggregationTransaction map(TransactionEntity transaction, String currencyCode) {
        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(transaction.getAmount(), currencyCode))
                .setDescription(transaction.getDescription())
                .setDate(transaction.getDate())
                .addExternalSystemIds(
                        TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                        transaction.getTransactionId())
                .setRawDetails(transaction.getRawDetails())
                .setProviderMarket(PROVIDER_MARKET)
                .build();
    }
}
