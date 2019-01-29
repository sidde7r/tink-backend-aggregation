package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.agents.rpc.Credentials;

public class AktiaCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {
    private static final AggregationLogger log = new AggregationLogger(AktiaCreditCardAccountFetcher.class);
    private final AktiaApiClient apiClient;

    private AktiaCreditCardAccountFetcher(AktiaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static AktiaCreditCardAccountFetcher create(AktiaApiClient apiClient) {
        return new AktiaCreditCardAccountFetcher(apiClient);
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        String productsSummary = apiClient.productsSummary();
        log.infoExtraLong(productsSummary, AktiaConstants.LogTags.PRODUCTS_SUMMARY);
        return Collections.emptyList();
    }
}
