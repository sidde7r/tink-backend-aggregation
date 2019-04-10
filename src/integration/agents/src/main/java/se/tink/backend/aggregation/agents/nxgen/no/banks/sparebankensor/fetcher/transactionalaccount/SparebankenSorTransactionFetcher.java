package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class SparebankenSorTransactionFetcher implements TransactionFetcher {
    private final SparebankenSorApiClient apiClient;

    public SparebankenSorTransactionFetcher(SparebankenSorApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(Account account) {

        Optional<HashMap<String, LinkEntity>> storedLinks =
                account.getFromTemporaryStorage(
                        SparebankenSorConstants.Storage.TEMPORARY_STORAGE_LINKS,
                        new TypeReference<HashMap<String, LinkEntity>>() {});

        // if no transactions link, return empty
        if (!storedLinks.isPresent()) {
            return Collections.emptyList();
        }

        HashMap<String, LinkEntity> links = storedLinks.get();

        List<TransactionEntity> transactions =
                apiClient
                        .fetchTransactions(
                                links.get(SparebankenSorConstants.Storage.TRANSACTIONS).getHref())
                        .getTransactions();

        if (transactions == null) {
            return Collections.emptyList();
        }

        return transactions.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
