package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
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
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.amount.ExactCurrencyAmount;

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
            final GenericCardsResponse cardsResponse = apiClient.fetchCards();

            // group cards by contract
            final Map<String, List<GenericCardEntity>> contracts =
                    cardsResponse.getCards().stream()
                            .filter(GenericCardEntity::isCreditCard)
                            .collect(Collectors.groupingBy(GenericCardEntity::getContract));

            // set balance for first card in contract, zero for the others
            return cardsResponse.getCards().stream()
                    .filter(GenericCardEntity::isCreditCard)
                    .map(card -> mapCreditCardAccount(card, contracts.get(card.getContract())))
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

    private CreditCardAccount mapCreditCardAccount(
            GenericCardEntity card, List<GenericCardEntity> cardsInContract) {
        boolean isFirstCardOnContract = cardsInContract.get(0).equals(card);
        if (!isFirstCardOnContract) {
            return card.toTinkCard(ExactCurrencyAmount.zero(LaCaixaConstants.CURRENCY));
        }
        try {
            // Prepaid cards have no contract, so we use their balance directly
            // Try to fetch the total debt from the next predicted settlement (COB-685, ESD-317)
            final ExactCurrencyAmount balance =
                    card.getRefValIdContract() != null
                            ? fetchBalanceForCardContract(card.getRefValIdContract())
                            : card.getAvailableCredit();

            return card.toTinkCard(balance);
        } catch (NoSuchElementException e) {
            // If there are no predicted settlements, sum disposed balances from contract
            // This matches what is shown in the overview of the app
            LOG.warn("Unable to fetch card balance");
            return card.toTinkCard(getBalanceForContract(cardsInContract));
        } catch (HttpResponseException hre) {
            if (isCurrentlyUnavailable(hre.getResponse())) {
                // For VIA T toll payment devices, we get a 409 in fetchCardLiquidations
                return card.toTinkCard(getBalanceForContract(cardsInContract));
            }
            throw hre;
        }
    }

    private ExactCurrencyAmount getBalanceForContract(List<GenericCardEntity> cards) {
        final BigDecimal sum =
                cards.stream()
                        .map(GenericCardEntity::getBalance)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        return ExactCurrencyAmount.of(sum, LaCaixaConstants.CURRENCY);
    }

    private ExactCurrencyAmount fetchBalanceForCardContract(String contractRefVal)
            throws NoSuchElementException {
        final CardLiquidationsResponse liquidations =
                apiClient.fetchCardLiquidations(contractRefVal, true);
        final String liquidationDateValue =
                liquidations
                        .getNextFutureLiquidationDate()
                        .orElseThrow(NoSuchElementException::new);
        final LiquidationDetailResponse liquidationDetail =
                apiClient.fetchCardLiquidationDetail(
                        liquidations.getRefValNumContract(), liquidationDateValue);
        final BigDecimal totalDebt = liquidationDetail.getLiquidationPeriod().getMyDebt();
        if (Objects.isNull(totalDebt)) {
            throw new NoSuchElementException();
        }
        return ExactCurrencyAmount.of(totalDebt, LaCaixaConstants.CURRENCY).negate();
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        // Pagination state is maintained on the server. We should only indicate if this is
        // new/first request or not.
        // The response contains a boolean that indicates if there is more data to fetch or not.

        // if there are no transactions we sometimes get an error response instead of empty, we
        // return empty
        try {
            return apiClient.fetchCardTransactions(account.getApiIdentifier(), page == 0);
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

    private boolean isCurrentlyUnavailable(HttpResponse response) {
        if (response != null && response.getStatus() == HttpStatus.SC_CONFLICT) {
            LaCaixaErrorResponse error = response.getBody(LaCaixaErrorResponse.class);
            return error != null && error.isCurrentlyUnavailable();
        }

        return false;
    }
}
