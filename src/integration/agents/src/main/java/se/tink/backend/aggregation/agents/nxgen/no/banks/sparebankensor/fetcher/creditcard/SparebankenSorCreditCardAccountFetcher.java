package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.creditcard;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class SparebankenSorCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {
    private static final AggregationLogger logger =
            new AggregationLogger(SparebankenSorCreditCardAccountFetcher.class);

    private final SparebankenSorApiClient apiClient;

    public SparebankenSorCreditCardAccountFetcher(SparebankenSorApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        try {
            apiClient.fetchCreditCards();
        } catch (Exception e) {
            logger.infoExtraLong(
                    "Failed to retrieve credit cards",
                    SparebankenSorConstants.LogTags.CREDIT_CARD_LOG_TAG,
                    e);
        }
        return Collections.emptyList();
    }
}
