package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.creditcard;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.creditcard.entities.CardTranscationEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.creditcard.rpc.CardTransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@AllArgsConstructor
public class SparebankenSorCreditCardTransactionFetcher implements TransactionFetcher {
    private final SparebankenSorApiClient apiClient;

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(Account account) {
        Optional<Map<String, LinkEntity>> storedLinks =
                account.getFromTemporaryStorage(
                        Storage.TEMPORARY_STORAGE_LINKS,
                        new TypeReference<Map<String, LinkEntity>>() {});

        // if no transactions link, return empty
        if (!storedLinks.isPresent()) {
            return Collections.emptyList();
        }

        Map<String, LinkEntity> links = storedLinks.get();

        List<CardTranscationEntity> transactions =
                apiClient
                        .fetchTransactions(
                                links.get(Storage.TRANSACTIONS).getHref(),
                                CardTransactionResponse.class)
                        .getCardTransactions();

        if (transactions == null) {
            return Collections.emptyList();
        }

        // Removes transactions that are missing executed amount
        transactions.removeIf(t -> t.getAmounts().isExecutedEntityNull());

        return transactions.stream()
                .map(CardTranscationEntity::toTinkCardTransaction)
                .collect(Collectors.toList());
    }
}
