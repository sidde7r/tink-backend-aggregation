package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.rpc.CardsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ImaginBankCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionPagePaginator<CreditCardAccount> {
    private static final AggregationLogger LOGGER = new AggregationLogger(ImaginBankCreditCardFetcher.class);

    private final ImaginBankApiClient apiClient;

    public ImaginBankCreditCardFetcher(ImaginBankApiClient bankApi) {
        this.apiClient = bankApi;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        try {
            apiClient.initiateCardFetching();

            String cardsResponseRaw = apiClient.fetchCards();
            LOGGER.infoExtraLong(cardsResponseRaw, ImaginBankConstants.LogTags.CREDIT_CARD);
            CardsResponse cardsResponse = SerializationUtils.deserializeFromString(cardsResponseRaw, CardsResponse.class);
            if (cardsResponse != null && cardsResponse.getCardList() != null) {
                for (CardEntity cardEntity : Optional.ofNullable(cardsResponse.getCardList().getCards())
                        .orElse(Collections.emptyList())) {

                    while (true) {
                        PaginatorResponse pr = fetchAndLogTransactions(cardEntity.getCardKey(), 0);
                        if (!pr.canFetchMore().orElse(false)) {
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.info("Failed to fetch cards", e);
        }

        return Collections.emptyList();
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        return new CardTransactionsResponse();
    }

    private PaginatorResponse fetchAndLogTransactions(String cardKey, int page) {
        // Pagination state is maintained on the server. We should only indicate if this is new/first request or not.
        // The response contains a boolean that indicates if there is more data to fetch or not.
        LocalDate fromDate = LocalDate.of(2013, 01, 01);
        LocalDate toDate = LocalDate.now();

        try {
            String cardTransactionsResponse = apiClient.fetchCardTransactions(cardKey, fromDate,
                    toDate, page > 0);
            LOGGER.infoExtraLong(cardTransactionsResponse, ImaginBankConstants.LogTags.CREDIT_CARD);
        } catch (Exception e) {
            LOGGER.info("Failed to fetch transactions for card", e);
        }

        return new CardTransactionsResponse();
    }
}
