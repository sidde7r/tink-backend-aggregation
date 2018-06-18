package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import org.apache.http.client.utils.URIBuilder;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.TargoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.TargoBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.utils.TargoBankUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.rpc.TransactionSummaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class TargoBankTransactionsFetcher implements TransactionFetcher<TransactionalAccount> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TargoBankTransactionsFetcher.class);
    private final TargoBankApiClient apiClient;
    private final SessionStorage sessionStorage;

    private TargoBankTransactionsFetcher(TargoBankApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public static TargoBankTransactionsFetcher create(TargoBankApiClient apiClient, SessionStorage sessionStorage) {
        return new TargoBankTransactionsFetcher(apiClient, sessionStorage);
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        String webId = account.getTemporaryStorage().get(TargoBankConstants.Tags.WEB_ID);
        Optional<TransactionSummaryResponse> transactionsForAccount = getTransactionsForAccount(webId);

        List<AggregationTransaction> transactions = Lists.newArrayList();
        transactionsForAccount.ifPresent(transactionList ->
                transactionList.getTransactions()
                        .stream().map(tr -> tr.toTransaction())
                        .forEach(t -> transactions.add(t))
        );
        return transactions;
    }

    private Optional<TransactionSummaryResponse> getTransactionsForAccount(String webId) {
        String body = buildTransactionSummaryRequest(webId);
        TransactionSummaryResponse details = apiClient.getTransactions(body);
        if (!TargoBankUtils.isSuccess(details.getReturnCode())) {
            return Optional.empty();
        }
        this.sessionStorage.put(TargoBankConstants.Tags.ACCOUNT_LIST, details);
        return Optional.of(details);
    }

    private String buildTransactionSummaryRequest(String webId) {
        URIBuilder uriBuilder = new URIBuilder();
        try {
            return uriBuilder
                    .addParameter(
                            TargoBankConstants.RequestBodyValues.WEB_ID,
                            webId)
                    .addParameter(
                            TargoBankConstants.RequestBodyValues.WS_VERSION,
                            "1")
                    .addParameter(
                            TargoBankConstants.RequestBodyValues.MEDIA,
                            TargoBankConstants.RequestBodyValues.MEDIA_VALUE)
                    .build()
                    .getQuery();
        } catch (URISyntaxException e) {
            LOGGER.error("Error building login body request\n", e);
            throw new RuntimeException(e);
        }
    }
}
