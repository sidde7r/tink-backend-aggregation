package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.fetcher.creditcard;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.SpankkiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.SpankkiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.fetcher.creditcard.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.fetcher.creditcard.rpc.CardsOverviewResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SpankkiCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionFetcher<CreditCardAccount> {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

            logger.info(
                    "tag={} {}",
                    SpankkiConstants.LogTags.LOG_TAG_CREDIT_CARD,
                    this.apiClient.fetchCardsOverview());

        } catch (HttpResponseException e) {
            logger.warn("tag={}", SpankkiConstants.LogTags.LOG_TAG_CREDIT_CARD, e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        return Collections.emptyList();
    }
}
