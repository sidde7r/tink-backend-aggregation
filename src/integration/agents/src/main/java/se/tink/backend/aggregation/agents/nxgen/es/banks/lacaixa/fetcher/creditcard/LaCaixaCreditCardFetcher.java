package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities.CardLiquidationDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities.GenericCardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc.CardLiquidationsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc.GenericCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc.LiquidationDetailResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.rpc.LaCaixaErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.libraries.amount.Amount;

public class LaCaixaCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionPagePaginator<CreditCardAccount> {
    private static final Logger LOG = LoggerFactory.getLogger(LaCaixaCreditCardFetcher.class);

    private final LaCaixaApiClient apiClient;

    public LaCaixaCreditCardFetcher(LaCaixaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        try {
            GenericCardsResponse cardsResponse = apiClient.fetchCards();
            return cardsResponse.getCards().stream()
                    .filter(GenericCardEntity::isCreditCard)
                    .map(card -> card.toTinkCard(fetchBalanceForCard(card)))
                    .collect(Collectors.toList());
        } catch (HttpResponseException hre) {

            HttpResponse response = hre.getResponse();

            if (response.getStatus() == HttpStatus.SC_CONFLICT) {
                LaCaixaErrorResponse errorResponse = response.getBody(LaCaixaErrorResponse.class);

                if (errorResponse.isUserHasNoOwnCards()) {
                    return Collections.emptyList();
                }
            }

            throw hre;
        }
    }

    private Amount fetchBalanceForCard(GenericCardEntity card) {
        final CardLiquidationsResponse liquidations =
                apiClient.fetchCardLiquidations(card.getRefValIdContract(), true);
        final CardLiquidationDataEntity liquidation = liquidations.getNextFutureLiquidation();
        final String liquidationDateValue = liquidation.getEndDate().getValue();
        final LiquidationDetailResponse liquidationDetail =
                apiClient.fetchCardLiquidationDetail(
                        liquidations.getRefValNumContract(), liquidationDateValue);
        return Amount.inEUR(liquidationDetail.getLiquidationPeriod().getMyDebt()).negate();
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        // Pagination state is maintained on the server. We should only indicate if this is
        // new/first request or not.
        // The response contains a boolean that indicates if there is more data to fetch or not.

        // if there are no transactions we sometimes get an error respomnse instead of empty, we
        // return empty
        try {
            return apiClient.fetchCardTransactions(account.getBankIdentifier(), page == 0);
        } catch (HttpResponseException hre) {
            if (noTransactions(hre.getResponse())) {
                LOG.info(
                        String.format(
                                "Failed to fetch transaction for credit card %s",
                                account.getAccountNumber()));
                return PaginatorResponseImpl.createEmpty(false);
            }

            throw hre;
        }
    }

    private boolean noTransactions(HttpResponse response) {
        if (response != null && response.getStatus() == HttpStatus.SC_CONFLICT) {
            LaCaixaErrorResponse error = response.getBody(LaCaixaErrorResponse.class);
            return error != null && error.isEmptyList();
        }

        return false;
    }
}
