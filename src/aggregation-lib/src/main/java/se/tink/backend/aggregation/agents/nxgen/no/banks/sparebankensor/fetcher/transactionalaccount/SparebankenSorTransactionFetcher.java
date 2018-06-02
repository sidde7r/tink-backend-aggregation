package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants.StaticUrlValuePairs;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SparebankenSorTransactionFetcher implements TransactionFetcher {
    private final SparebankenSorApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SparebankenSorTransactionFetcher(SparebankenSorApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(Account account) {
        Map transactionUrlsByAccount = SerializationUtils.deserializeFromString(
                sessionStorage.get(SparebankenSorConstants.Storage.ACCOUNT_TRANSACTION_URLS), Map.class);

        String urlEndPart = (String) transactionUrlsByAccount.get(account.getBankIdentifier());

        URL url = new URL(SparebankenSorConstants.Url.TRANSACTIONS_URL_START + urlEndPart)
                .queryParam(StaticUrlValuePairs.TRANSACTIONS_BATCH_SIZE.getKey(),
                            StaticUrlValuePairs.TRANSACTIONS_BATCH_SIZE.getValue())
                .queryParam(StaticUrlValuePairs.RESERVED_TRANSACTIONS.getKey(),
                            StaticUrlValuePairs.RESERVED_TRANSACTIONS.getValue());

        List<TransactionEntity> transactions = apiClient.fetchTransactions(url).getTransactions();

        if (transactions == null){
            return Collections.emptyList();
        }

        return transactions.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
