package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm;

import java.util.List;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class CreditMutuelPfmCreditCardTransactionsFetcher implements TransactionFetcher<CreditCardAccount> {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(
                    CreditMutuelPfmCreditCardTransactionsFetcher.class);
    private static final AggregationLogger AGGREGATION_LOGGER = new AggregationLogger(EuroInformationApiClient.class);
    private final EuroInformationApiClient apiClient;

    private CreditMutuelPfmCreditCardTransactionsFetcher(EuroInformationApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static CreditMutuelPfmCreditCardTransactionsFetcher create(EuroInformationApiClient apiClient) {
        return new CreditMutuelPfmCreditCardTransactionsFetcher(apiClient);
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        return Lists.emptyList();
    }
}
