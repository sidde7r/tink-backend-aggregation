package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.API.run;

import io.vavr.control.Try;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants.CardState;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants.Storage;
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

        Try<CardTransactionsResponse> response =
                Try.of(() -> bankClient.fetchCardTransactions(account.getApiIdentifier(), page))
                        .onFailure(HttpResponseException.class, handleHttpResponseExceptions())
                        .onSuccess(
                                cardTransactionsResponse ->
                                        cardTransactionsResponse.handleReturnCode());

        return response.get();
    }

    private Consumer<HttpResponseException> handleHttpResponseExceptions() {
        return e -> {
            final HttpResponse res = e.getResponse();

            Match(res.getStatus())
                    .of(
                            Case($(500), run(() -> handleBankSideError(e))),
                            Case(
                                    $(),
                                    () -> {
                                        throw e;
                                    }));
        };
    }

    private void handleBankSideError(HttpResponseException e) {
        throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
    }
}
