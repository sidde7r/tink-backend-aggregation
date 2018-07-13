package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class AktiaTransactionFetcher<A extends Account> implements TransactionFetcher<A> {
    private static final AggregationLogger log = new AggregationLogger(AktiaTransactionFetcher.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final AktiaApiClient apiClient;

    private AktiaTransactionFetcher(AktiaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static <A extends Account> AktiaTransactionFetcher<A> create(AktiaApiClient apiClient) {
        return new AktiaTransactionFetcher<>(apiClient);
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(A account) {
        String transactionResponseString = apiClient.transactions(account.getBankIdentifier());
        log.infoExtraLong(transactionResponseString, AktiaConstants.LogTags.TRANSACTIONS_RESPONSE);

        TransactionsResponse transactionsResponse;
        try {
            transactionsResponse = mapper.readValue(transactionResponseString, TransactionsResponse.class);
        } catch (IOException e) {
            log.warn("Could not deserialize transaction response", e);
            return Collections.emptyList();
        }

        List<TransactionEntity> transactions = transactionsResponse.getTransactions();

        if (transactions == null){
            return Collections.emptyList();
        }

        return transactions.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
