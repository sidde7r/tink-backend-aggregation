package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard;

import java.util.Collection;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants.CardState;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.error.ErrorsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.entities.AnswerEntityGlobalPositionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class EvoBancoCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionPagePaginator<CreditCardAccount> {

    private final EvoBancoApiClient bankClient;

    public EvoBancoCreditCardFetcher(EvoBancoApiClient bankClient) {
        this.bankClient = bankClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        GlobalPositionResponse globalPositionResponse = bankClient.globalPosition();

        if (globalPositionResponse != null) {
            globalPositionResponse.handleReturnCode();

            AnswerEntityGlobalPositionResponse answer =
                    globalPositionResponse.getEeOGlobalbePosition().getAnswer();

            if (answer.getAgreementsList() == null || answer.getAgreementsList().isEmpty()) {
                return Collections.emptyList();
            }

            return answer.getCreditCardAccounts();
        }

        return Collections.emptyList();
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        final String cardState = account.getFromTemporaryStorage(Storage.CARD_STATE);
        if (CardState.NOT_ACTIVATED.equalsIgnoreCase(cardState)) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        try {
            return bankClient.fetchCardTransactions(account.getApiIdentifier(), page);
        } catch (HttpResponseException e) {
            if (isEmptyTransactionsListResponse(e.getResponse())) {
                return PaginatorResponseImpl.createEmpty(false);
            }
            throw BankServiceError.DEFAULT_MESSAGE.exception(e);
        }
    }

    private boolean isEmptyTransactionsListResponse(HttpResponse httpResponse) {
        try {
            final CardTransactionsResponse response =
                    httpResponse.getBody(CardTransactionsResponse.class);
            ErrorsEntity errorCode = response.getErrors().get();
            return ErrorCodes.TRANSACTIONS_ERROR.equals(errorCode.getShowCode());
        } catch (HttpClientException ex) {
            // Evo Banco sometimes returns not a JSON response. It either does mean that end-user
            // doesn't have credit cards or it is another format of a credit card's transactions
            // empty list response.
            if (httpResponse.getStatus() == 400
                    && ex.getMessage()
                            .contains(
                                    "com.fasterxml.jackson.core.JsonParseException: Unexpected character")) {
                return true;
            }
        }
        return false;
    }
}
