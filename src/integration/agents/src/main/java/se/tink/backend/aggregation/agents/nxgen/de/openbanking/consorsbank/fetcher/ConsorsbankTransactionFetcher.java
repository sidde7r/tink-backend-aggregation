package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.fetcher;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.ConsorsbankStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.client.ConsorsbankFetcherApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities.FetcherLinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.mappers.TransactionMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@RequiredArgsConstructor
@Slf4j
public class ConsorsbankTransactionFetcher implements TransactionFetcher<TransactionalAccount> {

    private final ConsorsbankFetcherApiClient apiClient;
    private final ConsorsbankStorage storage;
    private final TransactionMapper consorsbankTransactionMapper;
    private final TransactionPaginationHelper transactionPaginationHelper;

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        if (!checkIfAllowedToFetchTransactions(account)) {
            log.info(
                    "Skipping fetching transactions. Available consent does not allow us to fetch them.");
            return Collections.emptyList();
        }

        FetchTransactionsResponse fetchTransactionsResponse;
        fetchTransactionsResponse =
                apiClient.fetchTransactions(
                        storage.getConsentId(), account.getApiIdentifier(), getStartDate(account));

        List<AggregationTransaction> result =
                consorsbankTransactionMapper.toTinkTransactions(
                        fetchTransactionsResponse.getTransactions());

        String nextUrl = extractNextUrl(fetchTransactionsResponse);
        while (nextUrl != null) {
            fetchTransactionsResponse =
                    apiClient.fetchTransactions(storage.getConsentId(), nextUrl);

            result.addAll(
                    consorsbankTransactionMapper.toTinkTransactions(
                            fetchTransactionsResponse.getTransactions()));
            nextUrl = extractNextUrl(fetchTransactionsResponse);
        }

        return result;
    }

    private boolean checkIfAllowedToFetchTransactions(TransactionalAccount account) {
        Optional<String> maybeIban = getIban(account);
        if (!maybeIban.isPresent()) {
            log.warn(
                    "Skipping fetching transactions. Did not find iban on account to compare to consent access list!");
            return false;
        }

        String iban = maybeIban.get();
        return Optional.ofNullable(storage.getConsentAccess().getTransactions())
                .orElse(Collections.emptyList()).stream()
                .map(AccountReferenceEntity::getIban)
                .filter(Objects::nonNull)
                .anyMatch(x -> x.equalsIgnoreCase(iban));
    }

    private Optional<String> getIban(TransactionalAccount account) {
        return account.getIdentifiers().stream()
                .filter(IbanIdentifier.class::isInstance)
                .map(IbanIdentifier.class::cast)
                .map(IbanIdentifier::getIban)
                .findAny();
    }

    private LocalDate getStartDate(TransactionalAccount account) {
        return transactionPaginationHelper
                .getTransactionDateLimit(account)
                .map(x -> x.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .orElse(LocalDate.ofEpochDay(0));
    }

    private String extractNextUrl(FetchTransactionsResponse transactionsResponse) {
        return Optional.ofNullable(transactionsResponse.getLinks())
                .map(FetcherLinksEntity::getNext)
                .map(Href::getHref)
                .orElse(null);
    }
}
