package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Localization.DATE_TIME_FORMATTER_TRANSACTIONS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Transactions;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Transactions.GetClient;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common.BasePolishApiGetClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishTransactionsApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling.PolishApiErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions.dto.responses.TransactionsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class PolishApiGetTransactionsClient extends BasePolishApiGetClient
        implements PolishApiTransactionClient {

    private final PolishTransactionsApiUrlFactory urlFactory;

    public PolishApiGetTransactionsClient(
            PolishTransactionsApiUrlFactory urlFactory,
            TinkHttpClient httpClient,
            AgentConfiguration<PolishApiConfiguration> configuration,
            AgentComponentProvider agentComponentProvider,
            PolishApiPersistentStorage persistentStorage) {
        super(httpClient, configuration, agentComponentProvider, persistentStorage);
        this.urlFactory = urlFactory;
    }

    @Override
    public TransactionsResponse fetchTransactionsByDate(
            String accountNumber,
            LocalDate from,
            LocalDate to,
            Transactions.TransactionTypeRequest transactionType) {

        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(
                                urlFactory.getTransactionsUrl(accountNumber), getTokenFromStorage())
                        .queryParam(
                                GetClient.QueryParams.TRANSACTION_DATE_FROM,
                                DATE_TIME_FORMATTER_TRANSACTIONS.format(from))
                        .queryParam(
                                GetClient.QueryParams.TRANSACTION_DATE_TO,
                                DATE_TIME_FORMATTER_TRANSACTIONS.format(to))
                        .queryParam(
                                GetClient.QueryParams.TRANSACTION_STATUS,
                                transactionType.name().toUpperCase())
                        .queryParam(
                                GetClient.QueryParams.PAGE_SIZE,
                                String.valueOf(Transactions.PAGE_SIZE));

        return PolishApiErrorHandler.callWithErrorHandling(
                requestBuilder, TransactionsResponse.class, PolishApiErrorHandler.RequestType.GET);
    }

    @Override
    public TransactionsResponse fetchTransactionsByNextPage(
            String nextPage,
            String accountNumber,
            LocalDate from,
            LocalDate to,
            Transactions.TransactionTypeRequest transactionType) {
        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(
                        urlFactory.getTransactionsContinuationUrl(nextPage), getTokenFromStorage());

        return PolishApiErrorHandler.callWithErrorHandling(
                requestBuilder, TransactionsResponse.class, PolishApiErrorHandler.RequestType.GET);
    }
}
