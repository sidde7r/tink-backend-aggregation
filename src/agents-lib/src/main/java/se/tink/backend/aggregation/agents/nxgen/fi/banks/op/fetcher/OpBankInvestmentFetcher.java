
package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc.PortfolioDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankPortfolioRootEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class OpBankInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static final AggregationLogger log = new AggregationLogger(OpBankInvestmentFetcher.class);

    private final OpBankApiClient client;
    private final Credentials credentials;

    public OpBankInvestmentFetcher(OpBankApiClient client, Credentials credentials) {
        this.client = client;
        this.credentials = credentials;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {

        log.infoExtraLong("Summary: " + client.fetchTradingAssetsSummary(),
                OpBankConstants.Fetcher.INVESTMENT_LOGGING);

        OpBankPortfolioRootEntity tradingAssetsPortfolios = client.fetchTradingAssetsPortfolios();

        return tradingAssetsPortfolios.getPortfolios().stream()
                .map(tradingAssetPortfolio -> client.fetchTradingAssetsPortfolioDetails(tradingAssetPortfolio.getPortfolioId()))
                .map(responseString -> logUnknownInstrumentGroupsAndParse(responseString))
                .filter(PortfolioDetailsResponse::hasInvestmentGroups)
                .map(PortfolioDetailsResponse::toTinkInvestmentAccount)
                .collect(Collectors.toList());
    }

    // this logging is until we have found all instrument group types
    private PortfolioDetailsResponse logUnknownInstrumentGroupsAndParse(String portfolioDetails) {
        Matcher instrumentGroupMatcher = OpBankConstants.Fetcher.EXTRACT_INSTRUMENT_GROUP_PATTERN
                .matcher(portfolioDetails);
        while (instrumentGroupMatcher.find()) {
            if (instrumentGroupMatcher.groupCount() > 0) {
                if (!OpBankConstants.Fetcher.KNOWN_PORTFOLIO_TYPES
                        .contains(instrumentGroupMatcher.group(1).toLowerCase())) {
                    log.infoExtraLong(String.format("Property name: %s, type: %s", instrumentGroupMatcher.group(0),
                                    instrumentGroupMatcher.group(1)),
                            OpBankConstants.Fetcher.INVESTMENT_PORTFOLIO_TYPE_LOGGING);
                }
            }
        }

        return SerializationUtils.deserializeFromString(portfolioDetails, PortfolioDetailsResponse.class);
    }
}
