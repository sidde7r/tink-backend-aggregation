package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.agents.rpc.Credentials;

public class AktiaInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static final AggregationLogger log = new AggregationLogger(AktiaInvestmentFetcher.class);
    private final AktiaApiClient apiClient;
    private final Credentials credentials;

    private AktiaInvestmentFetcher(AktiaApiClient apiClient, Credentials credentials) {
        this.apiClient = apiClient;
        this.credentials = credentials;
    }

    public static AktiaInvestmentFetcher create(AktiaApiClient apiClient, Credentials credentials) {
        return new AktiaInvestmentFetcher(apiClient, credentials);
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        String investments = apiClient.investments();
        log.infoExtraLong(investments, AktiaConstants.LogTags.INVESTMENTS);
        return Collections.emptyList();
    }
}
