package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Headers.HeaderKeys.X_REQUEST_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Localization.DATE_TIME_FORMATTER_TRANSACTIONS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Transactions;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common.BasePolishApiPostClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.concreteagents.PolishApiAgentCreator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishTransactionsApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling.PolishApiErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions.dto.requests.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions.dto.responses.TransactionsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

@Slf4j
public class PolishApiPostTransactionsClient extends BasePolishApiPostClient
        implements PolishApiTransactionClient {

    private final PolishTransactionsApiUrlFactory urlFactory;

    public PolishApiPostTransactionsClient(
            PolishApiAgentCreator polishApiAgentCreator,
            TinkHttpClient httpClient,
            AgentConfiguration<PolishApiConfiguration> configuration,
            AgentComponentProvider agentComponentProvider,
            PolishApiPersistentStorage persistentStorage) {
        super(
                httpClient,
                agentComponentProvider,
                configuration,
                persistentStorage,
                polishApiAgentCreator);
        this.urlFactory = polishApiAgentCreator.getTransactionsApiUrlFactory();
    }

    @Override
    public TransactionsResponse fetchTransactionsByDate(
            String accountNumber,
            LocalDate from,
            LocalDate to,
            Transactions.TransactionTypeRequest transactionType) {
        String requestId = getUuid();
        ZonedDateTime zonedDateTime = getNow();

        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(
                                urlFactory.getTransactionsUrl(transactionType),
                                zonedDateTime,
                                getTokenFromStorage())
                        .header(X_REQUEST_ID, requestId)
                        .body(
                                prepareTransactionsRequestBody(
                                        accountNumber, from, to, requestId, zonedDateTime),
                                MediaType.APPLICATION_JSON);

        return PolishApiErrorHandler.callWithErrorHandling(
                requestBuilder, TransactionsResponse.class, PolishApiErrorHandler.RequestType.POST);
    }

    @Override
    public TransactionsResponse fetchTransactionsByNextPage(
            String nextPage,
            String accountNumber,
            LocalDate from,
            LocalDate to,
            Transactions.TransactionTypeRequest transactionType) {
        String requestId = getUuid();
        ZonedDateTime zonedDateTime = getNow();

        RequestBuilder requestBuilder =
                getRequestWithBaseHeaders(
                                urlFactory.getTransactionsUrl(transactionType),
                                zonedDateTime,
                                getTokenFromStorage())
                        .header(X_REQUEST_ID, requestId)
                        .body(
                                prepareTransactionsContinuationRequestBody(
                                        nextPage,
                                        accountNumber,
                                        from,
                                        to,
                                        requestId,
                                        zonedDateTime),
                                MediaType.APPLICATION_JSON);

        return PolishApiErrorHandler.callWithErrorHandling(
                requestBuilder, TransactionsResponse.class, PolishApiErrorHandler.RequestType.POST);
    }

    private TransactionsRequest prepareTransactionsRequestBody(
            String accountNumber,
            LocalDate from,
            LocalDate to,
            String requestId,
            ZonedDateTime zonedDateTime) {
        return TransactionsRequest.builder()
                .requestHeader(
                        getRequestHeaderEntity(
                                requestId, zonedDateTime, getAccessTokenFromStorage()))
                .accountNumber(accountNumber)
                .transactionDateFrom(DATE_TIME_FORMATTER_TRANSACTIONS.format(from))
                .transactionDateTo(DATE_TIME_FORMATTER_TRANSACTIONS.format(to))
                .perPage(Transactions.PAGE_SIZE)
                .build();
    }

    private TransactionsRequest prepareTransactionsContinuationRequestBody(
            String nextPage,
            String accountNumber,
            LocalDate from,
            LocalDate to,
            String requestId,
            ZonedDateTime zonedDateTime) {
        return TransactionsRequest.builder()
                .requestHeader(
                        getRequestHeaderEntity(
                                requestId, zonedDateTime, getAccessTokenFromStorage()))
                .accountNumber(accountNumber)
                .transactionDateFrom(DATE_TIME_FORMATTER_TRANSACTIONS.format(from))
                .transactionDateTo(DATE_TIME_FORMATTER_TRANSACTIONS.format(to))
                .pageId(nextPage)
                .perPage(Transactions.PAGE_SIZE)
                .build();
    }
}
