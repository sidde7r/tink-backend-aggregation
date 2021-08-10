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
            HttpResponse httpResponse = e.getResponse();
            final CardTransactionsResponse response =
                    httpResponse.getBody(CardTransactionsResponse.class);
            ErrorsEntity errorCode =
                    response.getEeOConsultationMovementsTarjetabe().getError().get();

            if (ErrorCodes.TRANSACTIONS_ERROR.equals(errorCode.getShowCode())) {
                return PaginatorResponseImpl.createEmpty(false);
            }
            log.warn(
                    "Error message: httpStatus: {}, code: {}, message: {}",
                    httpResponse.getStatus(),
                    errorCode.getShowCode(),
                    errorCode.getMessageShow());

            throw BankServiceError.DEFAULT_MESSAGE.exception(e);
        }
    }
}
