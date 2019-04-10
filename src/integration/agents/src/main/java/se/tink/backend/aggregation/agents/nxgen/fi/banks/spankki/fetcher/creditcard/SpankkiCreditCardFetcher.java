package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.creditcard;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.SpankkiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.SpankkiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.creditcard.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.creditcard.rpc.CardsOverviewResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class SpankkiCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionFetcher<CreditCardAccount> {
    private static final AggregationLogger LOGGER =
            new AggregationLogger(SpankkiCreditCardFetcher.class);

    private final SpankkiApiClient apiClient;

    public SpankkiCreditCardFetcher(SpankkiApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {

        try {
            String rawResponse = this.apiClient.fetchCardsOverview();
            CardsOverviewResponse cardsOverviewResponse =
                    new ObjectMapper().readValue(rawResponse, CardsOverviewResponse.class);

            if (!cardsOverviewResponse.getCredits().isEmpty()) {
                return cardsOverviewResponse.getCredits().stream()
                        .filter(CreditCardEntity::isCredit)
                        .map(CreditCardEntity::toTinkAccount)
                        .collect(Collectors.toList());
            }

            LOGGER.infoExtraLong(
                    this.apiClient.fetchCardsOverview(),
                    SpankkiConstants.LogTags.LOG_TAG_CREDIT_CARD);

        } catch (HttpResponseException e) {
            LOGGER.warnExtraLong(SpankkiConstants.LogTags.LOG_TAG_CREDIT_CARD, e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        return Collections.emptyList();
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        List<AggregationTransaction> transactions = Collections.emptyList();
        return transactions;
    }
}
